package ovh.snipundercover.darkchallenge.logging;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import ovh.snipundercover.darkchallenge.DarkChallenge;

import java.util.Hashtable;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringJoiner;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * A wrapper class for plugin subclass logging.<br>
 * This class is a drop-in replacement for {@link Logger} (but does not extend it).<br>
 * To get a new {@link PluginLogger}, use {@link PluginLogger#getLogger(String)}.
 *
 * @see PluginLogger#getLogger(Class)
 */
@SuppressWarnings("unused")
public class PluginLogger {
	private static final int                       OFF_VAL       = Level.OFF.intValue();
	private static final int                       INFO_VAL      = Level.INFO.intValue();
	private static final Logger                    PLUGIN_LOGGER = DarkChallenge.getPlugin().getLogger();
	private static final Map<String, PluginLogger> LOGGER_LOOKUP = new Hashtable<>();
	
	@Getter
	private final @NotNull String name;
	@Getter @Setter
	private @NotNull       Level  level = Level.INFO;
	
	@NotNull
	public static PluginLogger getLogger(@NotNull Class<?> clazz) {
		return getLogger(clazz.getSimpleName());
	}
	
	@NotNull
	public static PluginLogger getLogger(@NotNull String name) {
		if (LOGGER_LOOKUP.containsKey(name))
			return LOGGER_LOOKUP.get(name);
		PluginLogger newLogger = new PluginLogger(name);
		LOGGER_LOOKUP.put(name, newLogger);
		return newLogger;
	}
	
	@NotNull
	public static Logger getPluginLogger() {
		return PLUGIN_LOGGER;
	}
	
	/**
	 * See the below methods for acquiring loggers.
	 *
	 * @see PluginLogger#getLogger(String)
	 * @see PluginLogger#getLogger(Class)
	 */
	private PluginLogger(@NotNull String name) {
		this.name = name;
	}
	
	//oh boy, here we go
	
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean isLoggable(@NotNull Level level) {
		int levelValue = this.level.intValue();
		return level.intValue() >= levelValue && levelValue != OFF_VAL;
	}
	
	//helper methods
	
	@NotNull
	private Level wrapLevel(@NotNull Level level) {
		return level.intValue() < INFO_VAL
				? Level.INFO
				: level;
	}
	
	@NotNull
	private String wrapMessage(@NotNull Level level, String msg) {
		return "[%s/%s] %s".formatted(name, level.getName(), msg);
	}
	
	//log implementation
	
	public void log(Level level, @NotNull Supplier<String> msg) {
		log(level, msg.get());
	}
	
	public void log(Level level, String msg, Object... params) {
		if (!isLoggable(level)) return;
		PLUGIN_LOGGER.log(wrapLevel(level), wrapMessage(level, msg), params);
	}
	
	public void log(Level level, String msg, Throwable thrown) {
		if (!isLoggable(level)) return;
		PLUGIN_LOGGER.log(wrapLevel(level), wrapMessage(level, msg), thrown);
	}
	
	public void log(Level level, Throwable thrown, @NotNull Supplier<String> msg) {
		log(level, msg.get(), thrown);
	}
	
	//logp implementations
	
	@SuppressWarnings("SpellCheckingInspection") //goddamn you spellchecker
	public void logp(Level level, String sourceClass, String sourceMethod, @NotNull Supplier<String> msg) {
		logp(level, sourceClass, sourceMethod, msg.get());
	}
	
	@SuppressWarnings("SpellCheckingInspection")
	public void logp(Level level, String sourceClass, String sourceMethod, String msg, Object... params) {
		if (!isLoggable(level)) return;
		PLUGIN_LOGGER.logp(wrapLevel(level), sourceClass, sourceMethod, wrapMessage(level, msg), params);
	}
	
	@SuppressWarnings("SpellCheckingInspection")
	public void logp(Level level, String sourceClass, String sourceMethod, String msg, Throwable thrown) {
		if (!isLoggable(level)) return;
		PLUGIN_LOGGER.logp(wrapLevel(level), sourceClass, sourceMethod, wrapMessage(level, msg), thrown);
	}
	
	@SuppressWarnings("SpellCheckingInspection")
	public void logp(Level level, String sourceClass, String sourceMethod, Throwable thrown, @NotNull Supplier<String> msg) {
		logp(level, sourceClass, sourceMethod, msg.get(), thrown);
	}
	
	//logrb implementations
	
	@SuppressWarnings("SpellCheckingInspection")
	public void logrb(Level level,
	                  String sourceClass,
	                  String sourceMethod,
	                  ResourceBundle bundle,
	                  String msg,
	                  Object... params) {
		if (!isLoggable(level)) return;
		PLUGIN_LOGGER.logrb(wrapLevel(level), sourceClass, sourceMethod, bundle, wrapMessage(level, msg), params);
	}
	
	@SuppressWarnings("SpellCheckingInspection")
	public void logrb(Level level, ResourceBundle bundle, String msg, Object... params) {
		if (!isLoggable(level)) return;
		PLUGIN_LOGGER.logrb(wrapLevel(level), bundle, wrapMessage(level, msg), params);
	}
	
	@SuppressWarnings("SpellCheckingInspection")
	public void logrb(Level level,
	                  String sourceClass,
	                  String sourceMethod,
	                  ResourceBundle bundle,
	                  String msg,
	                  Throwable thrown) {
		if (!isLoggable(level)) return;
		PLUGIN_LOGGER.logrb(wrapLevel(level), sourceClass, sourceMethod, bundle, wrapMessage(level, msg), thrown);
	}
	
	@SuppressWarnings("SpellCheckingInspection")
	public void logrb(Level level, ResourceBundle bundle, String msg, Throwable thrown) {
		if (!isLoggable(level)) return;
		PLUGIN_LOGGER.logrb(wrapLevel(level), bundle, wrapMessage(level, msg), thrown);
	}
	
	//entering implementation
	
	public void entering(String sourceClass, String sourceMethod, Object... params) {
		if (!isLoggable(Level.FINER)) return;
		StringJoiner joiner = new StringJoiner(" ");
		joiner.add("ENTRY");
		IntStream.range(0, params.length)
		         .mapToObj("{%d}"::formatted)
		         .forEach(joiner::add);
		logp(Level.FINER, sourceClass, sourceMethod, joiner.toString(), params);
	}
	
	//exiting implementation
	
	public void exiting(String sourceClass, String sourceMethod) {
		logp(Level.FINER, sourceClass, sourceMethod, "RETURN");
	}
	
	public void exiting(String sourceClass, String sourceMethod, Object result) {
		logp(Level.FINER, sourceClass, sourceMethod, "RETURN {0}", result);
	}
	
	//throwing implementation
	
	public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
		logp(Level.FINER, sourceClass, sourceMethod, "THROW", thrown);
	}
	
	//level name methods (but better)
	
	public void severe(String msg, Object... params) {
		log(Level.SEVERE, msg, params);
	}
	
	public void severe(@NotNull Supplier<String> msg, Object... params) {
		severe(msg.get(), params);
	}
	
	public void warning(String msg, Object... params) {
		log(Level.WARNING, msg, params);
	}
	
	public void warning(@NotNull Supplier<String> msg, Object... params) {
		warning(msg.get(), params);
	}
	
	public void info(String msg, Object... params) {
		log(Level.INFO, msg, params);
	}
	
	public void info(@NotNull Supplier<String> msg, Object... params) {
		info(msg.get(), params);
	}
	
	public void config(String msg, Object... params) {
		log(Level.CONFIG, msg, params);
	}
	
	public void config(@NotNull Supplier<String> msg, Object... params) {
		config(msg.get(), params);
	}
	
	public void fine(String msg, Object... params) {
		log(Level.FINE, msg, params);
	}
	
	public void fine(@NotNull Supplier<String> msg, Object... params) {
		fine(msg.get(), params);
	}
	
	public void finer(String msg, Object... params) {
		log(Level.FINER, msg, params);
	}
	
	public void finer(@NotNull Supplier<String> msg, Object... params) {
		finer(msg.get(), params);
	}
	
	public void finest(String msg, Object... params) {
		log(Level.FINEST, msg, params);
	}
	
	public void finest(@NotNull Supplier<String> msg, Object... params) {
		finest(msg.get(), params);
	}
}
