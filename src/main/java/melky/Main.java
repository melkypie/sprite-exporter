package melky;

import java.io.IOException;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main
{
	public static void main(String[] args) throws IOException
	{

		try
		{

			var cacheDownloader = new CacheDownloader(Env.OLD_CACHE, Env.NEW_CACHE);
			var cachePath = Path.of(Env.CACHE_FOLDER);

			// todo: maybe generate sprite diffs later
			var archive = cacheDownloader.downloadRevision(cachePath, cacheDownloader.getNewCache());
			var extractFolder = cachePath.resolve(cacheDownloader.getNewCache());
			Archive.untar(archive, extractFolder);

			Path spritePath = extractFolder.resolve(Env.SPRITE_FOLDER);
			SpriteDumper.dump(extractFolder, spritePath);

			SampleGenerator.generate(spritePath, Path.of(Env.PACK_FOLDER));
		}
		catch (Throwable e)
		{
			log.error("{}", e);
			System.exit(1);
		}

		// okhttp3 connection pool hangs after http2 requests without Exit signal
		System.exit(0);
	}
}
