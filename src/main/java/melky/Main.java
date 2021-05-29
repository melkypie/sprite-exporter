package melky;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import net.runelite.cache.fs.Store;
import net.runelite.cache.fs.flat.FlatStorage;

@Slf4j
public class Main
{
	private static final int EOF = -1;
	private static final String old_rev = System.getProperty("cache.old");
	private static final String new_rev = System.getProperty("cache.new");
	private static final String cache = System.getProperty("cache.folder");

	public static void main(String[] args) throws IOException
	{
		if (args[0].equals("dump"))
		{
			dumpSprites(old_rev);
			dumpSprites(new_rev);
		}
		else if (args[0].equals("build-sample"))
		{
			moveImages();
		}
	}

	public static void dumpSprites(String rev) throws IOException
	{
		File dumpDir = new File(cache + "/" + rev + "/sprites");

		try (Store store = new Store(new FlatStorage(new File(cache + "/" + rev))))
		{
			store.load();

			SpriteManager dumper = new SpriteManager(
				store
			);
			dumper.load();
			dumper.export(dumpDir);
		}

		log.info("Dumped to {}", dumpDir);
	}

	public static void moveImages() throws IOException
	{
		String inputFolder = System.getProperty("resources.inputFolder");
		String outputFolder = System.getProperty("resources.outputFolder");
		if (Strings.isNullOrEmpty(inputFolder) || Strings.isNullOrEmpty(outputFolder))
		{
			throw new RuntimeException("inputFolder and outputFolder need to be defined");
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
			File folder = createOrRetrieve(outputFolder + "/" + override.getFolder().toString().toLowerCase());
			File destinationSprite = new File(folder, override.toString().toLowerCase().replaceFirst(override.getFolder().toString().toLowerCase() + "_", "") + ".png");
			File sourceSprite = new File(inputFolder + "/" + override.getSpriteID() + "-0.png");

			if (sourceSprite.exists() && !(destinationSprite.exists() && fileContentEquals(sourceSprite, destinationSprite)))
			{
				Files.copy(sourceSprite, destinationSprite);
				log.info("Updated sprite " + override.name() + " (" + override.getSpriteID() + ")");
			}
		}
		File outputFolderFile = new File(outputFolder);
		loopDirectory(outputFolderFile.listFiles(), outputFolderFile.getName(), inputFolder, true);
	}

	private static File createOrRetrieve(final String target) throws IOException
	{
		File outputDir = new File(target);
		if (!outputDir.exists())
		{
			outputDir.mkdirs();
		}

		return outputDir;
	}

	private static boolean fileContentEquals(File file1, File file2) throws IOException
	{
		if (!file1.exists() || !file2.exists())
		{
			return false;
		}

		try (FileInputStream finput1 = new FileInputStream(file1);
			 FileInputStream finput2 = new FileInputStream(file2))
		{
			try (BufferedInputStream binput1 = new BufferedInputStream(finput1);
				 BufferedInputStream binput2 = new BufferedInputStream(finput2))
			{
				int b1 = binput1.read();
				while (EOF != b1)
				{
					int b2 = binput2.read();
					if (b1 != b2)
					{
						return false;
					}
					b1 = binput1.read();
				}
				return binput2.read() == EOF;
			}
		}
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
						SpriteOverride override;
						if (dirName.equalsIgnoreCase("other"))
						{
							override = SpriteOverride.valueOf(file.getName().replace(".png", "").toUpperCase());
						}
						else
						{
							override = SpriteOverride.valueOf(dirName.toUpperCase() + "_" + file.getName().replace(".png", "").toUpperCase());
						}

						if (override.getSpriteID() < 0)
						{
							continue;
						}

						File originalSprite = new File(spriteDir + "/" + override.getSpriteID() + "-0.png");
						if (!delete && fileContentEquals(file, originalSprite))
						{
							log.info("File " + file.getName() + " (" + override.getSpriteID() + ") in folder " + dirName + " is the same as the vanilla sprite");
						}
					}
					catch (IllegalArgumentException e)
					{
						if (delete)
						{
							file.delete();
						}
						else
						{
							log.info("File " + file.getName() + " in folder " + dirName + " is redundant");
						}
					}
				}
			}
		}
	}
}
