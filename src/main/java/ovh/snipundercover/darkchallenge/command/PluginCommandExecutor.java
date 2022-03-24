package ovh.snipundercover.darkchallenge.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import ovh.snipundercover.darkchallenge.DarkChallenge;
import ovh.snipundercover.darkchallenge.logging.PluginLogger;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * An interface for handling commands.<br>
 * Create a class extending {@link PluginCommandExecutor} inside the package this class is in.<br>
 * The class will automatically get picked up and registered as a command handler upon {@link Plugin#onEnable}
 */
public abstract class PluginCommandExecutor implements CommandExecutor {
	
	private static final Map<String, PluginCommandExecutor> COMMAND_HANDLERS = new Hashtable<>();
	private static final PluginLogger                       LOGGER           =
			PluginLogger.getLogger(PluginCommandExecutor.class);
	
	public static void initCommands(DarkChallenge plugin) {
		if (!COMMAND_HANDLERS.isEmpty())
			throw new IllegalStateException("Command handlers already initialized!");
		LOGGER.info("Initializing commands...");
		
		final var subclasses =
				new Reflections(PluginCommandExecutor.class.getPackageName())
						.getSubTypesOf(PluginCommandExecutor.class);
		LOGGER.fine("Found {0} handler classes.", subclasses.size());
		
		AtomicInteger count = new AtomicInteger();
		subclasses.forEach(clazz -> {
			LOGGER.fine("Attempting to initialize {0}...", clazz.getSimpleName());
			PluginCommandExecutor command;
			try {
				command = clazz.getConstructor().newInstance();
			} catch (NoSuchMethodException e) {
				//missing non-params constructor;
				LOGGER.warning("Failed to initialize {0}: class does not have a non-parameter constructor.",
				               clazz.getSimpleName()
				);
				return;
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
				//error while instantiating
				LOGGER.log(Level.WARNING,
				           "Failed to initialize %s: an error occurred while initializing."
						           .formatted(clazz.getSimpleName()),
				           e
				);
				return;
			}
			final PluginCommand ymlCommand = plugin.getCommand(command.name());
			if (ymlCommand == null) {
				//command missing from plugin.yml
				LOGGER.warning("Failed to initialize {0}: the command is missing from plugin.yml.",
				               clazz.getSimpleName()
				);
				return;
			}
			COMMAND_HANDLERS.put(ymlCommand.getName(), command);
			ymlCommand.setExecutor(command);
			count.getAndIncrement();
			LOGGER.fine("... done");
		});
		LOGGER.info("Initialized {0}/{1} command handlers.",
		            count.get(),
		            COMMAND_HANDLERS.size()
		);
	}
	
	@NotNull
	public abstract String name();
	
	//TODO make plugin.yml declaration optional
	
	public abstract void onCommand(@NotNull CommandSender sender, @NotNull String[] args);
	
	@Override
	public boolean onCommand(@NotNull CommandSender sender,
	                         @NotNull Command command,
	                         @NotNull String label,
	                         @NotNull String[] args) {
		onCommand(sender, args);
		return true;
	}
}
