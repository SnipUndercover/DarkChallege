package ovh.snipundercover.darkchallenge.util;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Utils {
	private Utils() {}
	
	@Contract(pure = true, value = "null -> new")
	@NotNull
	public static String color(@Nullable String s) {
		if (s == null || s.isEmpty()) return "";
		return ChatColor.translateAlternateColorCodes('&', s);
	}
	
	@Contract(pure = true, value = "null -> new")
	@NotNull
	public static String[] color(@Nullable String... s) {
		if (s == null || s.length == 0) return new String[0];
		return Arrays.stream(s)
		             .map(Utils::color)
		             .toArray(String[]::new);
	}
	
	@Contract(pure = true)
	@NotNull
	public static <T> Predicate<T> isParsable(@NotNull Function<T, ?> parse) {
		return isParsable(parse, null);
	}
	
	@Contract(pure = true)
	@NotNull
	public static <T> Predicate<T> isParsable(@NotNull Function<T, ?> parse, BiConsumer<T, Exception> onFail) {
		return (val) -> {
			try {
				parse.apply(val);
				return true;
			} catch (Exception e) {
				if (onFail != null) onFail.accept(val, e);
				return false;
			}
		};
	}
}
