package melky;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
@Getter
public class CacheDownloader
{
	private static final Gson gson = new Gson();

	String oldCache;
	String newCache;

	public CacheDownloader(String oldCache, String newCache) throws IOException
	{
		this.oldCache = oldCache;
		this.newCache = newCache;
		getVersions();
	}

	public CacheDownloader() throws IOException
	{
		getVersions();
	}

	public Path downloadRevision(Path outDir, String revision) throws IOException
	{
		Path outFile = outDir.resolve(revision + ".tar.gz");
		if (Files.exists(outFile))
		{
			log.info("{} already exists", outFile);
			return outFile;
		}

		Request request = new Request.Builder()
			.url("https://api.github.com/repos/abextm/osrs-cache/tarball/refs/tags/" + revision)
			.header("User-Agent", "Java-OkHttp-Client")
			.build();

		try (Response response = Env.CLIENT.newCall(request).execute())
		{
			if (!response.isSuccessful())
			{
				throw new IOException("HTTP request failed: " + response.code() + " - " + response.message());
			}

			log.info("Downloaded {}", revision);

			Files.createDirectories(outFile.getParent());
			try (InputStream inputStream = response.body().byteStream())
			{
				Files.copy(inputStream, outFile, StandardCopyOption.REPLACE_EXISTING);
			}

			log.info("Wrote file to {}", outFile);
		}

		return outFile;
	}

	private CacheDownloader getVersions() throws IOException
	{
		if (isValid(oldCache) && isValid(newCache))
		{
			return this;
		}

		String commitsJson = fetchCommits();
		JsonArray commits = gson.fromJson(commitsJson, JsonArray.class);

		if (!isValid(oldCache))
		{
			String commitMessage = commits.get(1)
				.getAsJsonObject()
				.getAsJsonObject("commit")
				.get("message").getAsString();
			this.oldCache = commitMessage.replace("Cache version ", "");
			log.info("Fetch old cache version: {}", this.oldCache);
		}

		if (!isValid(newCache))
		{
			String commitMessage = commits.get(0)
				.getAsJsonObject()
				.getAsJsonObject("commit")
				.get("message").getAsString();
			this.newCache = commitMessage.replace("Cache version ", "");
			log.info("Fetch new cache version: {}", this.newCache);
		}

		return this;
	}

	private static String fetchCommits() throws IOException
	{
		Request request = new Request.Builder()
			.url("https://api.github.com/repos/abextm/osrs-cache/commits")
			.header("Accept", "application/vnd.github.v3+json")
			.header("User-Agent", "Java-OkHttp-Client")
			.build();

		try (Response response = Env.CLIENT.newCall(request).execute())
		{
			if (!response.isSuccessful())
			{
				throw new IOException("HTTP request failed: " + response.code() + " - " + response.message());
			}

			return response.body().string();
		}
	}

	private static boolean isValid(String version)
	{
		return !Strings.isNullOrEmpty(version);
	}
}
