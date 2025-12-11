package melky;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.model.SpriteOverride;

@Slf4j
public class SampleGenerator
{
	/**
	 * Generates a sample-vanilla sprite pack from the given cache and cleans up missing files
	 *
	 * @param inputFolder  input cache
	 * @param outputFolder pack output folder
	 * @throws IOException
	 */
	public static void generate(@Nonnull Path inputFolder, @Nonnull Path outputFolder) throws IOException
	{
		if (!java.nio.file.Files.exists(inputFolder))
		{
			throw new IOException(inputFolder + " does not exist");
		}

		for (SpriteOverride override : SpriteOverride.values())
		{
			// TODO:
			// Grab the tag tab images from rl repo and make an exception for login screen
			// Figure out why 990 is so janky
			if (override.getSpriteID() < 0 || override.getSpriteID() == 990)
			{
				continue;
			}

			String name = override.getFolder() + "";
			File destinationSprite = outputFolder.resolve(Path.of(name.toLowerCase(), override.toString().toLowerCase().replaceFirst(name.toLowerCase() + "_", "") + ".png")).toFile();
			com.google.common.io.Files.createParentDirs(destinationSprite);

			String fileName = override.getSpriteID() + "-" + (override.getFrameID() != -1 ? override.getFrameID() : 0) + ".png";
			File sourceSprite = inputFolder.resolve(fileName).toFile();

			if (sourceSprite.exists() &&
				!(destinationSprite.exists() && Files.equal(sourceSprite, destinationSprite)))
			{
				Files.copy(sourceSprite, destinationSprite);
				log.info("Updated sprite {} ({})", override.name(), override.getSpriteID());
			}
		}

		// Delete images from pack that are missing from SpriteOverride
		File outputFolderFile = outputFolder.toFile();
		loopDirectory(outputFolderFile.listFiles(), outputFolderFile.getName(), inputFolder + "", true);
	}

	private static SpriteOverride fileToOverride(@Nonnull File file)
	{
		String parentFolder = file.getParentFile().getName().toLowerCase();
		String fileName = file.getName().replace(".png", "");

		if ("other".equals(parentFolder))
		{
			return SpriteOverride.valueOf(fileName.toUpperCase());
		}

		return SpriteOverride.valueOf((parentFolder + "_" + fileName).toUpperCase());
	}

	private static void loopDirectory(File[] directory, String dirName, String spriteDir, boolean delete) throws IOException
	{
		if (directory == null)
		{
			return;
		}

		for (File file : directory)
		{
			if (file.isDirectory())
			{
				loopDirectory(file.listFiles(), file.getName(), spriteDir, delete);
			}
			else
			{
				if (file.getName().contains(".png") && !file.getName().equals("icon.png"))
				{
					try
					{
						SpriteOverride override = fileToOverride(file);
						if (override.getSpriteID() < 0)
						{
							continue;
						}

						String spriteFileName = override.getSpriteID() + "-" + (override.getFrameID() != -1 ? override.getFrameID() : 0) + ".png";
						File originalSprite = Path.of(spriteDir, spriteFileName).toFile();

						if (!delete && Files.equal(file, originalSprite))
						{
							log.info("File {} ({}) in folder {} is the same as the vanilla sprite", file.getName(), override.getSpriteID(), dirName);
						}
					}
					catch (IllegalArgumentException e)
					{
						if (delete)
						{
							file.delete();
							log.info("Deleted missing file: {}/{}", dirName, file.getName());
						}
						else
						{
							log.info("File {} in folder {} is redundant", file.getName(), dirName);
						}
					}
				}
			}
		}
	}
}
