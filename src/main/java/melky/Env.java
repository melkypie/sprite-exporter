package melky;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Env
{
	public final String OLD_CACHE = System.getProperty("cache.old");
	public final String NEW_CACHE = System.getProperty("cache.new");
	public final String CACHE_FOLDER = System.getProperty("cache.folder", "cache");
	public final String PACK_FOLDER = System.getProperty("cache.pack", "sample-vanilla");
	public final String SPRITE_FOLDER = System.getProperty("cache.sprites", "sprites");
}
