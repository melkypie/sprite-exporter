package melky;

import com.google.common.io.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

public class Archive
{
	public static void untar(Path source, Path destination) throws IOException
	{
		try (TarArchiveInputStream tis = new TarArchiveInputStream(
			new GZIPInputStream(new FileInputStream(source.toFile()))))
		{
			TarArchiveEntry entry;
			while ((entry = tis.getNextTarEntry()) != null)
			{
				String path = entry.getName();

				// Strip first component (like --strip 1)
				String[] parts = path.split("/", 2);
				String outFile = parts.length < 2 ? parts[0] : parts[1];

				File f = destination.resolve(outFile).toFile();
				if (entry.isDirectory())
				{
					f.mkdirs();
				}
				else
				{
					Files.createParentDirs(f);
					try (FileOutputStream fos = new FileOutputStream(f))
					{
						tis.transferTo(fos);
					}
				}
			}
		}
	}
}
