package ovh.snipundercover.darkchallenge.util;

import org.bukkit.Material;

import java.util.EnumSet;
import java.util.Set;

public final class Constants {
	private Constants() {}
	
	public static final int TICKS_PER_SECOND = 20;
	
	public static final Set<Material> WATERLOGGED_MATERIALS = EnumSet.of(
			Material.BUBBLE_COLUMN,
			Material.KELP,
			Material.KELP_PLANT,
			Material.SEAGRASS,
			Material.TALL_SEAGRASS,
			Material.WATER
	);
	
	public static final byte MAX_SAFE_SKY_LIGHT = 11;
}
