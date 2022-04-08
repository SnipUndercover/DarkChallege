package ovh.snipundercover.darkchallenge.task;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import ovh.snipundercover.darkchallenge.logging.PluginLogger;

import java.util.Objects;
import java.util.logging.Level;

@SuppressWarnings("unused") //used via reflections
public final class DurabilityPercentageTask extends PluginTask {
	
	public static final  String       OBJECTIVE_NAME = "durability";
	private static final PluginLogger LOGGER         = PluginLogger.getLogger(DurabilityPercentageTask.class);
	private static final Scoreboard   scoreboard     =
			Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();
	
	private              int throttle     = 0;
	private static final int MAX_THROTTLE = 20;
	
	//only allow instantiation from PluginTask
	//we can get instance via PluginTask.getTask(Class<? extends PluginTask>)
	DurabilityPercentageTask() {}
	
	static {
		if (getObjective() == null) {
			LOGGER.fine("\"{0}\" objective missing, creating.", OBJECTIVE_NAME);
			scoreboard.registerNewObjective(OBJECTIVE_NAME, "dummy", "% durability")
			          .setDisplaySlot(DisplaySlot.BELOW_NAME);
		} else LOGGER.fine("\"{0}\" objective already exists.", OBJECTIVE_NAME);
	}
	
	void init() {
		throttle = 0;
	}
	
	@Override
	public void run() {
		Bukkit.getOnlinePlayers().forEach(player -> {
			final ItemStack helmet = player.getInventory().getHelmet();
			final String playerName = player.getName();
			final Score score = getObjective().getScore(playerName);
			if (helmet == null) {
				if (throttle == MAX_THROTTLE)
					LOGGER.fine("{0} has no helmet.", playerName);
				score.setScore(0);
				return;
			}
			final int maxDurability = helmet.getType().getMaxDurability();
			final ItemMeta helmetMeta = helmet.hasItemMeta()
					? helmet.getItemMeta()
					: Bukkit.getItemFactory().getItemMeta(helmet.getType());
			assert helmetMeta != null;
			if (throttle == MAX_THROTTLE)
				LOGGER.finest("Helmet type: {0}, max durability: {1}, unbreakable: {2}, is Damageable: {3}",
				              helmet.getType(),
				              maxDurability,
				              helmetMeta.isUnbreakable(),
				              helmetMeta instanceof Damageable
				);
			if (maxDurability == 0
					|| helmetMeta.isUnbreakable()
					|| !(helmetMeta instanceof Damageable damageableItemMeta)) {
				if (throttle == MAX_THROTTLE)
					LOGGER.fine("{0}'s helmet can not take damage.", playerName);
				score.setScore(100);
				return;
			}
			int currentDurability = damageableItemMeta.getDamage();
			int percentage = (maxDurability - currentDurability) * 100 / maxDurability;
			if (throttle == MAX_THROTTLE)
				LOGGER.finer("Helmet has {0}/{1} damage ({2}%)",
				             maxDurability - currentDurability,
				             maxDurability,
				             percentage
				);
			score.setScore(percentage);
			if (throttle == MAX_THROTTLE) throttle = 0;
			else throttle++;
		});
	}
	
	@Override
	void cleanup() {
		LOGGER.fine("Unregistering objective \"{0}\"...");
		try {
			getObjective().unregister();
		} catch (IllegalStateException e) {
			LOGGER.log(Level.FINER, "Encountered exception while unregistering:", e);
			LOGGER.fine("Objective \"{0}\" already unregistered, moving on.");
		}
	}
	
	public static Objective getObjective() {
		return scoreboard.getObjective(OBJECTIVE_NAME);
	}
}
