package melky;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import net.runelite.cache.fs.Store;
import net.runelite.cache.fs.flat.FlatStorage;

@Slf4j
public class SpriteDumper
{
	public static void dump(Path cacheDir, Path dumpDir) throws IOException
	{
		var spriteDir = dumpDir.toFile();
		if (!Files.exists(dumpDir))
		{
			Files.createDirectories(dumpDir);
		}

		try (Store store = new Store(new FlatStorage(cacheDir.toFile())))
		{
			store.load();

			SpriteManager dumper = new SpriteManager(store);
			dumper.load();
			dumper.export(spriteDir);
		}

		log.info("Dumped to {}", spriteDir);
	}
}
