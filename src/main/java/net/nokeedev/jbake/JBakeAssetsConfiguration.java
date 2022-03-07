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

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.DocsType;
import org.gradle.api.attributes.Usage;

final class JBakeAssetsConfiguration implements Action<Configuration> {
	static final String JBAKE_ASSETS_USAGE_NAME = "jbake-assets";
	private final Project project;

	public JBakeAssetsConfiguration(Project project) {
		this.project = project;
	}

	@Override
	public void execute(Configuration configuration) {
		configuration.attributes(it -> {
			it.attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, JBAKE_ASSETS_USAGE_NAME));
			it.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, project.getObjects().named(DocsType.class, JBAKE_ASSETS_USAGE_NAME));
		});
	}
}
