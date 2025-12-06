package io.ryoung;

import lombok.extern.slf4j.Slf4j;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;

@Slf4j
public class JavaDownloaderPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		JavaDownloadExtension extension = project.getExtensions()
			.create("javaDownload", JavaDownloadExtension.class);

		extension.getSourceUrl().convention("");
		extension.getTargetPath().convention("");

		TaskProvider<DownloadJavaTask> downloadTask = project.getTasks()
			.register("downloadJavaFiles", DownloadJavaTask.class, task -> {
				task.getSourceUrl().set(extension.getSourceUrl());
				task.getTargetPath().set(extension.getTargetPath());
				task.getTargetFile().set(project.getLayout().getProjectDirectory().file(extension.getTargetPath()));
			});

		project.afterEvaluate(p -> {
			log.info("JavaDownloaderPlugin: Configuring downloads for IDE sync...");

			String targetPath = extension.getTargetPath().get();
			if (targetPath.isEmpty()) {
				log.info("JavaDownloaderPlugin: Target path not configured");
				return;
			}

			java.io.File targetFile = project.file(targetPath);
			if (!targetFile.exists()) {
				try {
					downloadTask.get().download();
					log.info("Successfully downloaded: {}", targetFile.getName());
				} catch (Exception e) {
					log.error("Download during configuration failed: {}", e.getMessage());
				}
			} else {
				log.info("Java file already exists: {}", targetFile.getName());
			}

			var sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
			sourceSets.getByName("main").getJava().srcDir(project.file("src/main/java"));
		});
	}
}