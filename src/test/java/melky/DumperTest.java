package melky;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.Test;

public class DumperTest
{
	@Test
	public void dumpSprites() throws IOException
	{
		var path = Path.of("out", Env.CACHE_FOLDER, UntarTest.CACHE_VERSION);
		SpriteDumper.dump(path, path.resolve(Env.SPRITE_FOLDER));
	}
}
