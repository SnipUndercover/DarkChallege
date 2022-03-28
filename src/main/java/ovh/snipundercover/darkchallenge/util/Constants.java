package ovh.snipundercover.darkchallenge.util;

import org.bukkit.Material;

import java.util.List;

public final class Constants {
	private Constants() {}
	
	public static final int            TICKS_PER_SECOND      = 20;
	public static final List<Material> WATERLOGGED_MATERIALS = List.of(
			Material.BUBBLE_COLUMN,
			Material.KELP,
			Material.KELP_PLANT,
			Material.SEAGRASS,
			Material.TALL_SEAGRASS,
			Material.WATER
	);
	public static final byte MAX_SAFE_SKY_LIGHT = 11;
}
