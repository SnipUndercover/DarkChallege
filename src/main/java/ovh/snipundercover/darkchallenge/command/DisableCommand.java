package ovh.snipundercover.darkchallenge.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ovh.snipundercover.darkchallenge.DarkChallenge;
import ovh.snipundercover.darkchallenge.permission.PermissionNode;
import ovh.snipundercover.darkchallenge.util.Utils;

public class DisableCommand extends PluginCommandExecutor {
	
	public static final  String         COMMAND_NAME = "disable";
	private static final PermissionNode PERMISSION   = DarkChallenge.getPermissionRoot().getSubPermission(COMMAND_NAME);
	
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
		sender.sendMessage(Utils.color("&eDisabling plugin."));
		Bukkit.getPluginManager().disablePlugin(DarkChallenge.getPlugin());
	}
}
