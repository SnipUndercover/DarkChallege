package ovh.snipundercover.darkchallenge.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ovh.snipundercover.darkchallenge.DarkChallenge;
import ovh.snipundercover.darkchallenge.logging.PluginLogger;
import ovh.snipundercover.darkchallenge.permission.PermissionNode;
import ovh.snipundercover.darkchallenge.task.IgnitePlayersTask;
import ovh.snipundercover.darkchallenge.task.PluginTask;
import ovh.snipundercover.darkchallenge.util.Utils;

public class ReloadCommand extends PluginCommandExecutor {
	
	public static final  String         COMMAND_NAME = "disable";
	private static final PermissionNode PERMISSION   = DarkChallenge.getPermissionRoot().getSubPermission(COMMAND_NAME);
	private static final PluginLogger   LOGGER       = PluginLogger.getLogger(ReloadCommand.class);
	
	@Override
	@NotNull
	public String name() {
		return COMMAND_NAME;
	}
	
	@Override
	public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
		if (!PERMISSION.hasPermission(sender)) {
			sender.sendMessage(Utils.color("&cMissing permission %s.".formatted(PERMISSION.getPermissionString())));
			return;
		}
		sender.sendMessage(Utils.color("&eReloading configuration..."));
		LOGGER.info("{0} requested configuration reload.", sender.getName());
		
		LOGGER.fine("Reloading from disk...", sender.getName());
		DarkChallenge.getInstance().reloadConfig();
		
		LOGGER.fine("Reloading IgnitePlayersTask...", sender.getName());
		PluginTask.getTask(IgnitePlayersTask.class).reload();
		
		sender.sendMessage(Utils.color("&a...done."));
	}
}
