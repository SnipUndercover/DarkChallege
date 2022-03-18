package ovh.snipundercover.darkchallenge.util;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class Utils {
	private Utils() {}
	
	@Contract(pure = true, value = "null -> new")
	public static @NotNull String color(@Nullable String s) {
		if (s == null || s.isEmpty()) return "";
		return ChatColor.translateAlternateColorCodes('&', s);
	}
	
	@Contract(pure = true, value = "null -> new")
	public static String @NotNull [] color(String @Nullable ... s) {
		if (s == null || s.length == 0) return new String[0];
		return Arrays.stream(s)
		             .map(Utils::color)
		             .toArray(String[]::new);
	}
}
