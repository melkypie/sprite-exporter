package io.ryoung;

import lombok.Getter;
import org.gradle.api.provider.Property;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

@Getter
public abstract class JavaDownloadExtension {
	private final Property<String> sourceUrl;
	private final Property<String> targetPath;

	@Inject
	public JavaDownloadExtension(ObjectFactory objectFactory) {
		this.sourceUrl = objectFactory.property(String.class);
		this.targetPath = objectFactory.property(String.class);
	}
}