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

//import dev.gradleplugins.dockit.common.TaskNameFactory;

import java.util.Optional;

interface Names /*extends TaskNameFactory*/ {
	String configurationName(String name);
	String taskName(String name);
	String taskName(String verb, String object);
	Optional<String> capability();

	static Names forMain() {
		return new Names() {
			@Override
			public String configurationName(String name) {
				return name;
			}

			@Override
			public String taskName(String name) {
				return name;
			}

			@Override
			public String taskName(String verb, String object) {
				return verb + capitalized(object);
			}

			@Override
			public Optional<String> capability() {
				return Optional.empty(); // use default capability
			}

			private String capitalized(String s) {
				return Character.toUpperCase(s.charAt(0)) + s.substring(1);
			}
		};
	}

	static Names forName(String name) {
		return new Names() {
			@Override
			public String configurationName(String configurationName) {
				return name + capitalized(configurationName);
			}

			@Override
			public String taskName(String taskName) {
				return taskName + capitalized(name);
			}

			@Override
			public String taskName(String verb, String object) {
				return verb + capitalized(name) + capitalized(object);
			}

			@Override
			public Optional<String> capability() {
				return Optional.of(name);
			}

			private String capitalized(String s) {
				return Character.toUpperCase(s.charAt(0)) + s.substring(1);
			}
		};
	}
}
