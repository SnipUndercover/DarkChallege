package ovh.snipundercover.darkchallenge;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import ovh.snipundercover.darkchallenge.command.PluginCommandExecutor;
import ovh.snipundercover.darkchallenge.permission.PermissionNode;
import ovh.snipundercover.darkchallenge.task.PluginTask;

public final class DarkChallenge extends JavaPlugin {
	
	private static DarkChallenge instance;
	
	@Getter
	private static PermissionNode permissionRoot;
	
	@Override
	public void onEnable() {
		instance = this;
		permissionRoot = new PermissionNode(this.getName().toLowerCase());
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
