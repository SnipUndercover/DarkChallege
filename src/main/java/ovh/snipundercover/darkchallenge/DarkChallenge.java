package ovh.snipundercover.darkchallenge;

import org.bukkit.plugin.java.JavaPlugin;
import ovh.snipundercover.darkchallenge.command.PluginCommandExecutor;
import ovh.snipundercover.darkchallenge.task.PluginTask;

public final class DarkChallenge extends JavaPlugin {
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		PluginCommandExecutor.initCommands(this);
		PluginTask.startAll();
	}
	
	@Override
	public void onDisable() {
		PluginTask.stopAll();
	}
	
	public static DarkChallenge getPlugin() {
		return getPlugin(DarkChallenge.class);
	}
}
