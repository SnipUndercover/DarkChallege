package ovh.snipundercover.darkchallenge.task;

import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.reflections.Reflections;
import ovh.snipundercover.darkchallenge.DarkChallenge;
import ovh.snipundercover.darkchallenge.logging.PluginLogger;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * An interface for creating Bukkit tasks with ease.
 */
public abstract class PluginTask extends BukkitRunnable {
	private static final Map<Class<? extends PluginTask>, PluginTask> TASKS  = new Hashtable<>();
	private static final PluginLogger                                 LOGGER =
			PluginLogger.getLogger(PluginTask.class);
	
	static {
		LOGGER.fine("Initializing plugin tasks.");
		final Set<Class<? extends PluginTask>> availableTaskClasses =
				new Reflections(PluginTask.class.getPackageName()).getSubTypesOf(PluginTask.class);
		LOGGER.fine("Found {0} task classes.", availableTaskClasses.size());
		
		AtomicInteger count = new AtomicInteger();
		availableTaskClasses.forEach(clazz -> {
			try {
				LOGGER.finer("Attempting to initialize {0}...", clazz.getSimpleName());
				TASKS.put(clazz, clazz.getConstructor().newInstance());
				count.getAndIncrement();
				LOGGER.finer("... done");
			} catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
				//should never happen
				LOGGER.log(Level.WARNING,
				           "An error occurred while initializing task %s:".formatted(clazz.getSimpleName()), e
				);
			} catch (NoSuchMethodException e) {
				//should never happen
				LOGGER.log(Level.WARNING,
				           "An error occurred while initializing task {0}: "
						           + "the class does not have an accessible non-params constructor.",
				           clazz.getSimpleName()
				);
			}
		});
		LOGGER.fine("Initialized {0}/{1} tasks successfully.",
		            count.get(),
		            availableTaskClasses.size()
		);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends PluginTask> T getTask(Class<T> clazz) {
		return (T) TASKS.get(clazz); //no worries, it's fine
	}
	
	public static void startAll() {
		LOGGER.fine("Starting plugin tasks...");
		AtomicInteger count = new AtomicInteger();
		TASKS.values().forEach(task -> {
			try {
				task.start();
				count.getAndIncrement();
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Failed to start task %s:".formatted(task.getClass().getSimpleName()), e);
			}
		});
		LOGGER.fine("Started {0}/{1} plugin tasks.",
		            count.get(),
		            TASKS.size()
		);
	}
	
	public static void stopAll() {
		LOGGER.fine("Stopping plugin tasks...");
		AtomicInteger count = new AtomicInteger();
		TASKS.values().forEach(task -> {
			try {
				task.stop();
				count.getAndIncrement();
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Failed to stop task %s:".formatted(task.getClass().getSimpleName()), e);
			}
		});
		LOGGER.fine("Stopped {0}/{1} plugin tasks.",
		            count.get(),
		            TASKS.size()
		);
	}
	
	//non-static
	@Getter
	protected BukkitTask task;
	
	void init() {}
	
	void cleanup() {}
	
	@SuppressWarnings("UnusedReturnValue")
	public BukkitTask start() {
		return start(0L, 1L);
	}
	
	public BukkitTask start(long delay, long period) {
		LOGGER.finer("Starting task {0}...", getClass().getSimpleName());
		init();
		try {
			BukkitTask task = this.runTaskTimer(DarkChallenge.getPlugin(), delay, period);
			LOGGER.finer("...done. Task ID: {0}", task.getTaskId());
			return this.task = task;
		} catch (Exception e) {
			LOGGER.log(Level.WARNING,
			           "An error occurred while starting task %s:".formatted(getClass().getSimpleName()),
			           e
			);
			throw e;
		}
	}
	
	public void stop() {
		LOGGER.finer("Stopping task {0}...", getClass().getSimpleName());
		try {
			task.cancel();
			task = null;
			cleanup();
			LOGGER.finer("...done.");
		} catch (Exception e) {
			LOGGER.log(Level.WARNING,
			           "An error occurred while stopping task %s:".formatted(getClass().getSimpleName()),
			           e
			);
			throw e;
		}
	}
}
