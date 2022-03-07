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

import java.util.LinkedHashMap;
import java.util.Map;

final class MergeJBakePropertiesTransformer implements Transformer<Map<String, Object>, Iterable<Map<String, Object>>> {
	@Override
	public Map<String, Object> transform(Iterable<Map<String, Object>> allProperties) {
		final Map<String, Object> result = new LinkedHashMap<>();
		allProperties.forEach(properties -> {
			properties.forEach((k, v) -> {
				if (result.containsKey(k)) {
					throw new RuntimeException(String.format("Duplicated key '%s'.", k));
				}
				result.put(k, v);
			});
		});
		return result;
	}
}
