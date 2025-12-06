package melky;

import java.io.IOException;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class SampleTest
{
	@Test
	public void makeSample() throws IOException
	{
		Path cacheDir = Path.of("out", Env.CACHE_FOLDER, UntarTest.CACHE_VERSION, Env.SPRITE_FOLDER);
		Path packDir = Path.of("out", Env.PACK_FOLDER);

		SampleGenerator.generate(cacheDir, packDir);
	}
}
