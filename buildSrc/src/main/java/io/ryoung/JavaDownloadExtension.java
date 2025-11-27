package io.ryoung;

import org.gradle.api.provider.Property;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class JavaDownloadExtension {

	private final Property<String> sourceUrl;
	private final Property<String> targetPath;

	@Inject
	public JavaDownloadExtension(ObjectFactory objectFactory) {
		this.sourceUrl = objectFactory.property(String.class);
		this.targetPath = objectFactory.property(String.class);
	}

	public Property<String> getSourceUrl() {
		return sourceUrl;
	}

	public Property<String> getTargetPath() {
		return targetPath;
	}
}