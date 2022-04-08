package ovh.snipundercover.darkchallenge.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RangeTest {
	
	private static final Class<IllegalArgumentException> EXCEPTION_CLASS = IllegalArgumentException.class;
	
	private static final String EXCEPTION_STRING = "The beginning of this range is after its end. (%s > %s)";
	
	@Test
	@DisplayName("Check that start is before end")
	public void createRange() {
		assertThrows(EXCEPTION_CLASS, () -> new Range<>(1, -1), EXCEPTION_STRING.formatted(1, -1));
	}
	
	@Test
	@DisplayName("Ensure values are in range")
	public void isInRange() {
		final Range<Integer> range = new Range<>(0, 5);
		for (int i = 0; i <= 5; i++)
			assertTrue(range.isInRange(i));
		assertFalse(range.isInRange(-1));
		assertFalse(range.isInRange(6));
	}
}