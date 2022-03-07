/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.nokeedev.jbake;

import org.gradle.api.Transformer;
import org.gradle.api.file.FileSystemLocation;

import java.io.*;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

final class LoadPropertiesFileIfAvailable implements /*Callable<Map<String, Object>>,*/ Transformer<Map<String, Object>, FileSystemLocation> {
//	private final File propertiesFile;
//
//	public LoadPropertiesFileIfAvailable(File propertiesFile) {
//		this.propertiesFile = propertiesFile;
//	}
//
//	@Override
//	public Map<String, Object> call() throws Exception {
//		if (!propertiesFile.exists()) {
//			return Collections.emptyMap();
//		}
//
//		final Properties properties = new Properties();
//		try (InputStream inStream = new FileInputStream(propertiesFile)) {
//			properties.load(inStream);
//		}
//		final Map<String, Object> result = new LinkedHashMap<>();
//		properties.forEach((key, value) -> result.put(key.toString(), value));
//		return result;
//	}

	@Override
	public Map<String, Object> transform(FileSystemLocation propertiesFile) {
		return transform(propertiesFile.getAsFile());
	}

	private Map<String, Object> transform(File propertiesFile) {
		if (!propertiesFile.exists()) {
			return Collections.emptyMap();
		}

		final Properties properties = new Properties();
		try (InputStream inStream = new FileInputStream(propertiesFile)) {
			properties.load(inStream);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		final Map<String, Object> result = new LinkedHashMap<>();
		properties.forEach((key, value) -> result.put(key.toString(), value));
		return result;
	}
}
