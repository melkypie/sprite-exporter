package io.ryoung;

import lombok.extern.slf4j.Slf4j;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public abstract class DownloadJavaTask extends DefaultTask {

	@Input
	public abstract Property<String> getSourceUrl();

	@OutputFile
	public abstract RegularFileProperty getTargetFile();

	@Input
	public abstract Property<String> getTargetPath();

	@TaskAction
	public void executeDownload() {
		download();
	}

	public void download() {
		String url = getSourceUrl().get();
		if (url.isEmpty()) {
			throw new GradleException("Source URL is not configured");
		}

		java.io.File targetFile = getTargetFile().get().getAsFile();

		try {
			if (!targetFile.exists()) {
				log.info("Downloading Java file from: {}", url);

				com.google.common.io.Files.createParentDirs(targetFile);

				HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
				connection.setRequestProperty("User-Agent", "Gradle Build Tool");
				connection.setRequestProperty("Accept", "text/plain");

				int responseCode = connection.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK) {
					try (InputStream input = connection.getInputStream();) {
						com.google.common.io.Files.asByteSink(targetFile).writeFrom(input);
					}
					log.info("Successfully downloaded: {}", targetFile.getName());
				} else {
					throw new GradleException("Failed to download file. HTTP response: " + responseCode);
				}
			} else {
				log.info("Java file already exists: {}",  targetFile.getName());
			}
		} catch (IOException e) {
			throw new GradleException("Failed to download Java file: " + e.getMessage(), e);
		}
	}
}