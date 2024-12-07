package net.nokeedev.jbake;

import org.gradle.api.provider.Provider;

import java.util.Map;

public interface JBakeProperties {
	Provider<Map<String, Object>> getElements();
	Provider<Map<String, Object>> getAllElements();
}
