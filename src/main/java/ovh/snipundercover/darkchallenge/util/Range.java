package ovh.snipundercover.darkchallenge.util;

import org.jetbrains.annotations.NotNull;

public record Range<T extends Comparable<T>>(@NotNull T start, @NotNull T stop) {
	
	public Range {
		if (start.compareTo(stop) > 0)
			throw new IllegalArgumentException(
					"The beginning of this range is after its end. (%s > %s)".formatted(start, stop)
			);
	}
	
	public boolean isInRange(@NotNull T val) {
		return val.compareTo(start) >= 0 && val.compareTo(stop) <= 0;
	}
	
	@Override
	public String toString() {
		return "[%s; %s]".formatted(start, stop);
	}
}
