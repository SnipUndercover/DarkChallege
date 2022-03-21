package ovh.snipundercover.darkchallenge.task;

import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import ovh.snipundercover.darkchallenge.logging.PluginLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ovh.snipundercover.darkchallenge.util.Constants.TICKS_PER_SECOND;
import static ovh.snipundercover.darkchallenge.util.Constants.WATERLOGGED_MATERIALS;

@SuppressWarnings("unused")
@Deprecated
//TODO reimplement
public final class IgnitePlayersTask extends PluginTask {
	public static final  int                MIN_SAFE_TIME           = 12500; //ticks
	public static final  int                MAX_SAFE_TIME           = 23500; //ticks
	public static final  int                FIRE_LENGTH             = 8 * TICKS_PER_SECOND; //seconds
	public static final  int                HELMET_DAMAGE_DELAY     = 4 * TICKS_PER_SECOND; //seconds
	public static final  List<GameMode>     AFFECTED_GAMEMODES      = List.of(GameMode.SURVIVAL, GameMode.ADVENTURE);
	public static final  List<Environment>  AFFECTED_DIMENSIONS     = List.of(Environment.NORMAL);
	private static final PluginLogger       LOGGER                  =
			PluginLogger.getLogger(IgnitePlayersTask.class);
	private final        Map<UUID, Integer> playerHelmetDamageTicks = new HashMap<>();
	private static final int                MAX_THROTTLE            = 20;
	private              int                throttle                = 0;
	
	
	//only allow instantiation from PluginTask
	//we can get instance via PluginTask.getTask(Class<? extends PluginTask>)
	IgnitePlayersTask() {}
	
	public void init() {
		throttle = 0;
	}
	
	@Override
	public void run() {
		if (isCancelled()) return;
		Bukkit.getOnlinePlayers().forEach(player -> {
			final String playerName = player.getName();
			
			//ignore if the player is in creative/spectator
			final GameMode gameMode = player.getGameMode();
			if (!AFFECTED_GAMEMODES.contains(gameMode)) {
				if (throttle == MAX_THROTTLE) {
					LOGGER.fine("Ignoring {0}: outside of affected gamemode.", playerName);
					LOGGER.finer("Player has gamemode {0}.", gameMode);
				}
				return;
			}
			
			final World world = player.getWorld();
			
			//don't do anything if we're not in the affected dimensions
			final Environment dimension = world.getEnvironment();
			if (!AFFECTED_DIMENSIONS.contains(dimension)) {
				if (throttle == MAX_THROTTLE) {
					LOGGER.fine("Ignoring {0}: outside of affected dimension.", playerName);
					LOGGER.finer("Player has dimension {0}.", dimension);
				}
				return;
			}
			
			//don't do anything if it's raining/snowing/thundering
			if (!world.isClearWeather()) {
				if (throttle == MAX_THROTTLE)
					LOGGER.fine("Ignoring {0}: weather is not sunny.", playerName);
				return;
			}
			
			final long time = world.getTime();
			
			//don't do anything if it's night
			if (time >= MIN_SAFE_TIME && time <= MAX_SAFE_TIME) {
				if (throttle == MAX_THROTTLE) {
					LOGGER.fine("Ignoring {0}: it is night.", playerName);
					LOGGER.finer("World time for player is {0} (between [{1}, {2}]).",
					             time,
					             MIN_SAFE_TIME,
					             MAX_SAFE_TIME
					);
				}
				return;
			}
			
			final Location location = player.getLocation();
			final Block block = location.getBlock();
			
			//don't do anything if the player's in the dark
			final byte skyLight = block.getLightFromSky();
			if (skyLight <= 11) {
				if (throttle == MAX_THROTTLE) {
					LOGGER.fine("Ignoring {0}: player is in the dark.", playerName);
					LOGGER.finer("Player has light level {0}.", skyLight);
				}
				return;
			}
			
			//don't do anything if there's a block above the player
			final int highestY = world.getHighestBlockYAt(location);
			final int curY = location.getBlockY();
			if (highestY >= curY) {
				if (throttle == MAX_THROTTLE) {
					LOGGER.fine("Ignoring {0}: there is a block above.", playerName);
					LOGGER.finer("Player is at {0}; highest block Y is at {1}.",
					             curY,
					             highestY
					);
				}
				return;
			}
			
			//don't do anything if the player's in water
			final Material type = block.getType();
			final boolean isWaterOrWaterlogged = WATERLOGGED_MATERIALS.contains(type)
					|| block.getBlockData() instanceof Waterlogged waterloggedBlocKData
					&& waterloggedBlocKData.isWaterlogged();
			if (isWaterOrWaterlogged) {
				if (throttle == MAX_THROTTLE) {
					LOGGER.fine("Ignoring {0}: player is in water or a waterlogged block.", playerName);
					LOGGER.finer("Player is inside {0}.", type);
				}
				return;
			}
			
			final UUID uuid = player.getUniqueId();
			final PlayerInventory inventory = player.getInventory();
			final ItemStack helmet = inventory.getHelmet();
			
			if (helmet != null) {
				if (throttle == MAX_THROTTLE) {
					LOGGER.fine("Ignoring {0}: player has a helmet.", playerName);
				}
				
				//damage the helmet if possible
				final int maxDurability = helmet.getType().getMaxDurability();
				final ItemMeta helmetMeta = helmet.hasItemMeta()
						? helmet.getItemMeta()
						: Bukkit.getItemFactory().getItemMeta(helmet.getType());
				assert helmetMeta != null;
				if (maxDurability != 0
						&& !helmetMeta.isUnbreakable()
						&& helmetMeta instanceof Damageable damageable) {
					if (throttle == MAX_THROTTLE)
						LOGGER.finer("Player's helmet is damageable.", playerName);
					
					int unbreakingAmplifier = helmet.getEnchantmentLevel(Enchantment.DURABILITY) + 1;
					if (throttle == MAX_THROTTLE)
						LOGGER.finer("Unbreaking level: {0}.", unbreakingAmplifier);
					
					//wait HELMET_DAMAGE_DELAY * unbreakingAmplifier seconds between damage
					int currentTicks = playerHelmetDamageTicks.getOrDefault(uuid, 0) + 1;
					if (currentTicks < HELMET_DAMAGE_DELAY * unbreakingAmplifier) {
						if (throttle == MAX_THROTTLE)
							LOGGER.finest("Waiting to deal damage: {0}/{1} ticks.",
							              currentTicks,
							              HELMET_DAMAGE_DELAY * unbreakingAmplifier
							);
					} else {
						int newDamage = damageable.getDamage() + 1;
						if (throttle == MAX_THROTTLE)
							LOGGER.finer("Damaged helmet to {0}/{1}.",
							             maxDurability - newDamage,
							             maxDurability
							);
						if (maxDurability > newDamage) {
							damageable.setDamage(newDamage);
							helmet.setItemMeta(damageable);
						} else {
							if (throttle == MAX_THROTTLE)
								LOGGER.finer("Helmet damage below threshold, breaking. ({0}/{1})",
								             maxDurability - newDamage,
								             maxDurability
								);
							inventory.setHelmet(null);
							world.playSound(player, Sound.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1, 1);
						}
						currentTicks = 0;
					}
					playerHelmetDamageTicks.put(uuid, currentTicks);
				}
				return; //don't do anything if the player has a helmet on
			}
			if (throttle == MAX_THROTTLE)
				LOGGER.fine("No constraints met for {0}, igniting.", playerName);
			playerHelmetDamageTicks.remove(uuid); //player lost/took helmet off
			player.setFireTicks(FIRE_LENGTH);
			if (throttle == MAX_THROTTLE) throttle = 0;
			else throttle++;
		});
	}
}
