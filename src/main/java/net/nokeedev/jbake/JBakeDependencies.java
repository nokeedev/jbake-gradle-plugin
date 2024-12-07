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

import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.attributes.Usage;

public final class JBakeDependencies {
	public static final String JBAKE_CONFIGURATION_NAME = "jbake";

	public static final String ASSETS_CONFIGURATION_NAME = "assets";
	public static final String TEMPLATES_CONFIGURATION_NAME = "templates";
	public static final String CONTENT_CONFIGURATION_NAME = "content";
	public static final String CONFIGURATION_CONFIGURATION_NAME = "configuration";

	public static final String ASSETS_ELEMENTS_CONFIGURATION_NAME = "assetsElements";
	public static final String TEMPLATES_ELEMENTS_CONFIGURATION_NAME = "templatesElements";
	public static final String CONTENT_ELEMENTS_CONFIGURATION_NAME = "contentElements";
	public static final String CONFIGURATION_ELEMENTS_CONFIGURATION_NAME = "configurationElements";
	public static final String BAKED_ELEMENTS_CONFIGURATION_NAME = "bakedElements";

	private final NamedDomainObjectProvider<Configuration> bakedElements;
	private final NamedDomainObjectProvider<Configuration> jbakeElements;

	private final NamedDomainObjectProvider<Configuration> jbake;

	public JBakeDependencies(Project project, Names names) {
		final ConfigurationContainer configurations = project.getConfigurations();
		this.jbake = configurations.register(names.configurationName(JBAKE_CONFIGURATION_NAME),
			new AsDeclarable(new ConfigureJBakeExtensionDescription("JBake dependencies", it -> {})));

		this.jbakeElements = configurations.register(names.configurationName("jbakeElements"),
			new AsConsumable(new ConfigureJBakeExtensionDescription("JBake elements", it -> {})));
		this.jbakeElements.configure(config -> {
			config.extendsFrom(jbake.get());
			config.attributes(attributes -> {
				attributes.attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "jbake"));
			});
		});
		this.bakedElements = configurations.register(names.configurationName(BAKED_ELEMENTS_CONFIGURATION_NAME),
			new AsConsumable(new ExcludeFromAssembleTask(new ConfigureJBakeExtensionDescription("Baked elements", new JBakeBakedConfiguration(project)))));
	}

	public NamedDomainObjectProvider<Configuration> getBakedElements() {
		return bakedElements;
	}

	public NamedDomainObjectProvider<Configuration> getJBakeElements() {
		return jbakeElements;
	}

	public NamedDomainObjectProvider<Configuration> getJBake() {
		return jbake;
	}
}
