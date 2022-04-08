package ovh.snipundercover.darkchallenge;

import org.bukkit.plugin.java.JavaPlugin;
import ovh.snipundercover.darkchallenge.command.PluginCommandExecutor;
import ovh.snipundercover.darkchallenge.task.PluginTask;

public final class DarkChallenge extends JavaPlugin {
	
	private static DarkChallenge instance;
	
	@Override
	public void onEnable() {
		instance = this;
		saveDefaultConfig();
		PluginCommandExecutor.initCommands(this);
		PluginTask.startAll();
	}
	
	@Override
	public void onDisable() {
		PluginTask.stopAll();
	}
	
	public static DarkChallenge getPlugin() {
		return instance;
	}
}
