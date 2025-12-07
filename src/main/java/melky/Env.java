package melky;

import java.util.concurrent.TimeUnit;
import lombok.experimental.UtilityClass;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;

@UtilityClass
public class Env
{
	public final String OLD_CACHE = System.getProperty("cache.old");
	public final String NEW_CACHE = System.getProperty("cache.new");
	public final String CACHE_FOLDER = System.getProperty("cache.folder", "cache");
	public final String PACK_FOLDER = System.getProperty("cache.pack", "sample-vanilla");
	public final String SPRITE_FOLDER = System.getProperty("cache.sprites", "sprites");

	public final OkHttpClient CLIENT = new OkHttpClient.Builder()
		.addInterceptor(chain ->
		{
			Request request = chain.request();
			if (request.header("User-Agent") != null)
			{
				return chain.proceed(request);
			}

			Request userAgentRequest = request
				.newBuilder()
				.header("User-Agent", "ResourcePacks/Sprite-Exporter")
				.build();
			return chain.proceed(userAgentRequest);
		})
		.connectionPool(new ConnectionPool(0, 10, TimeUnit.SECONDS))
		.build();

}
