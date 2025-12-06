package melky;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.Test;

public class UntarTest
{
	public static String CACHE_VERSION = "2025-11-26-rev235";

	@Test
	public void untar() throws IOException
	{
		var cacheLoader = new CacheDownloader("abc", CACHE_VERSION);
		var archive = cacheLoader.downloadRevision(Path.of("out"), cacheLoader.getNewCache());

		Archive.untar(archive, Path.of("out", Env.CACHE_FOLDER, CACHE_VERSION));
	}
}
