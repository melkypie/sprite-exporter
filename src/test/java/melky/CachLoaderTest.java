package melky;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.Assert;
import org.junit.Test;

public class CachLoaderTest
{
	@Test
	public void providedVersions() throws IOException
	{
		CacheDownloader cl = new CacheDownloader("abc", "abc1");
		Assert.assertNotSame("Versions should be different", cl.getNewCache(), cl.getOldCache());
	}

	@Test
	public void fetchedVersions() throws IOException
	{
		CacheDownloader cl = new CacheDownloader();
		Assert.assertNotSame("Versions should be different", cl.getNewCache(), cl.getOldCache());
	}

	@Test
	public void downloadCache() throws IOException
	{
		CacheDownloader cl = new CacheDownloader();
		cl.downloadRevision(Path.of("out"), cl.getNewCache());
	}
}
