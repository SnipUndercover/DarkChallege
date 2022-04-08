package ovh.snipundercover.darkchallenge.task;

import lombok.Getter;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import ovh.snipundercover.darkchallenge.DarkChallenge;
import ovh.snipundercover.darkchallenge.logging.PluginLogger;
import ovh.snipundercover.darkchallenge.util.Constants;
import ovh.snipundercover.darkchallenge.util.Range;
import ovh.snipundercover.darkchallenge.util.Utils;

import java.util.*;
import java.util.stream.Collectors;

public class IgnitePlayersTask extends PluginTask {
	
	private static final PluginLogger LOGGER = PluginLogger.getLogger(IgnitePlayersTask.class);
	
	private final   Set<GameMode>    affectedGamemodes  = new HashSet<>();
	private final   Set<Environment> affectedDimensions = new HashSet<>();
	private @Getter Range<Long>      safeTime;
	private @Getter int              burnLength;
	private @Getter int              interval;
	private @Getter boolean          ignoreUnbreaking;
	
	private static final int MAX_THROTTLE = 20;
	private              int throttle     = 0;
	
	private final Map<UUID, Integer> helmetDamageTicks = new Hashtable<>();
	
	//only allow instantiation from PluginTask
	//we can get instance via PluginTask.getTask(Class<? extends PluginTask>)
	IgnitePlayersTask() {}
	
	@Override
	void init() {
		reload();
		this.throttle = 0;
	}
	
	@Override
	public void reload() {
		LOGGER.info("Reloading {0}...", this.getClass().getSimpleName());
		FileConfiguration config = DarkChallenge.getInstance().getConfig();
		
		Set<GameMode> affectedGamemodes =
				config.getStringList("affectedGamemodes")
				      .stream()
				      .map(String::toUpperCase)
				      .filter(Utils.isParsable(
						      GameMode::valueOf,
						      (name, e) -> LOGGER.warning(
								      "Found unknown gamemode \"{0}\", ignoring. Did you spell it right?",
								      name
						      )
				      ))
				      .map(GameMode::valueOf)
				      .collect(Collectors.toSet());
		LOGGER.config("Affected gamemodes: {0}", affectedGamemodes);
		this.affectedGamemodes.clear();
		this.affectedGamemodes.addAll(affectedGamemodes);
		
		Set<Environment> affectedDimensions =
				config.getStringList("affectedDimensions")
				      .stream()
				      .map(String::toUpperCase)
				      .map(name -> name.equals("OVERWORLD") ? "NORMAL" : name)
				      .filter(Utils.isParsable(
						      Environment::valueOf,
						      (name, e) -> LOGGER.warning(
								      "Found unknown dimension \"{0}\", ignoring. Did you spell it right?",
								      name
						      )
				      ))
				      .map(Environment::valueOf)
				      .collect(Collectors.toSet());
		LOGGER.config("Affected gamemodes: {0}", affectedGamemodes);
		this.affectedDimensions.clear();
		this.affectedDimensions.addAll(affectedDimensions);
		
		ConfigurationSection safeTime = config.getConfigurationSection("safeTime");
		assert safeTime != null;
		final long from = safeTime.getLong("from");
		final long to = safeTime.getLong("to");
		LOGGER.config("Safe time: {0} - {1}", from, to);
		this.safeTime = new Range<>(from, to);
		
		this.burnLength = config.getInt("burnLength");
		LOGGER.config("Burn length: {0} ({1} seconds)",
		              this.burnLength, "%.2f".formatted(this.burnLength / 20f)
		);
		
		ConfigurationSection helmetDamage = config.getConfigurationSection("helmetDamage");
		assert helmetDamage != null;
		this.interval = helmetDamage.getInt("interval");
		if (this.interval <= 0) LOGGER.config("Helmet damage interval: {0}, disabled.", this.interval);
		else LOGGER.config("Helmet damage interval: {0} ({1} seconds)",
		                   this.interval, "%.2f".formatted(this.interval / 20f)
		);
		
		this.ignoreUnbreaking = helmetDamage.getBoolean("ignoreUnbreaking");
		LOGGER.config("Ignore unbreaking: " + (this.ignoreUnbreaking ? "yes" : "no"));
		
		LOGGER.info("...done.");
	}
	
	@Override
	void run() {
		Bukkit.getOnlinePlayers().forEach(player -> {
			final String playerName = player.getName();
			
			//ignore if the player is in creative/spectator
			final GameMode gameMode = player.getGameMode();
			if (!affectedGamemodes.contains(gameMode)) {
				if (throttle == MAX_THROTTLE) {
					LOGGER.fine("Ignoring {0}: outside of affected gamemode.", playerName);
					LOGGER.finer("Player has gamemode {0}.", gameMode);
				}
				return;
			}
			
			final World world = player.getWorld();
			
			//don't do anything if we're not in the affected dimensions
			final Environment dimension = world.getEnvironment();
			if (!affectedDimensions.contains(dimension)) {
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
			if (safeTime.isInRange(time)) {
				if (throttle == MAX_THROTTLE) {
					LOGGER.fine("Ignoring {0}: it is night.", playerName);
					LOGGER.finer("World time for player is {0} (between {1}).", time, safeTime);
				}
				return;
			}
			
			final Location location = player.getLocation();
			final Block block = location.getBlock();
			
			//don't do anything if the player's in the dark
			final byte skyLight = block.getLightFromSky();
			if (skyLight <= Constants.MAX_SAFE_SKY_LIGHT) {
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
			final boolean isWaterOrWaterlogged = Constants.WATERLOGGED_MATERIALS.contains(type)
					|| block.getBlockData() instanceof Waterlogged waterloggedBlockData
					&& waterloggedBlockData.isWaterlogged();
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
			
			if (helmet == null) { //burn the player; no constraints met
				if (throttle == MAX_THROTTLE)
					LOGGER.fine("No constraints met for {0}, igniting.", playerName);
				helmetDamageTicks.remove(uuid);
				player.setFireTicks(burnLength);
				return;
			}
			
			//player has a helmet on, don't burn
			
			if (throttle == MAX_THROTTLE)
				LOGGER.fine("Ignoring {0}: player has a helmet.", playerName);
			
			if (interval <= 0) {
				if (throttle == MAX_THROTTLE)
					LOGGER.fine("Helmet damage disabled, moving on.");
				return;
			}
			
			//damage the helmet if possible
			final int maxDurability = helmet.getType().getMaxDurability();
			final ItemMeta helmetMeta = helmet.getItemMeta();
			assert helmetMeta != null;
			
			if (maxDurability == 0
					|| helmetMeta.isUnbreakable()
					// every ItemMeta must extend from CraftMetaItem, which itself extends from Damageable
					// except for Material#AIR, which of course returns null
					// meaning the below will always be !true, even for items that do not have durability
					// well then ¯\_(._.)_/¯
					|| !(helmetMeta instanceof Damageable damageable))
				return; //we can't damage the helmet, we're done here
			
			if (throttle == MAX_THROTTLE)
				LOGGER.finer("Player's helmet is damageable.", playerName);
			
			int unbreakingAmplifier = helmet.getEnchantmentLevel(Enchantment.DURABILITY) + 1;
			if (throttle == MAX_THROTTLE)
				LOGGER.finer("Unbreaking level: {0}.", unbreakingAmplifier - 1);
			
			//wait HELMET_DAMAGE_DELAY * unbreakingAmplifier seconds between damage
			int currentTicks = helmetDamageTicks.getOrDefault(uuid, 0) + 1;
			if (currentTicks >= interval * unbreakingAmplifier) {
				//time to damage
				int newDamage = damageable.getDamage() + 1;
				if (throttle == MAX_THROTTLE)
					LOGGER.finer("Damaging helmet to {0}/{1}.",
					             maxDurability - newDamage,
					             maxDurability
					);
				if (maxDurability > newDamage) {
					//helmet still alive
					damageable.setDamage(newDamage);
					helmet.setItemMeta(damageable);
				} else {
					//helmet about to break
					if (throttle == MAX_THROTTLE)
						LOGGER.finer("Helmet damage below threshold, breaking.");
					inventory.setHelmet(null);
					world.playSound(player, Sound.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1, 1);
				}
				currentTicks = 0;
			} else if (throttle == MAX_THROTTLE)
				//still waiting
				LOGGER.finest("Waiting to deal damage: {0}/{1} ticks.",
				              currentTicks,
				              interval * unbreakingAmplifier
				);
			helmetDamageTicks.put(uuid, currentTicks);
		});
		if (throttle == MAX_THROTTLE) throttle = 0;
		else throttle++;
	}
	
	public Set<GameMode> getAffectedGamemodes() {
		return Collections.unmodifiableSet(affectedGamemodes);
	}
	
	public Set<Environment> getAffectedDimensions() {
		return Collections.unmodifiableSet(affectedDimensions);
	}
	
}
