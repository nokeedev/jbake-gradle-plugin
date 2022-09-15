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

	private final NamedDomainObjectProvider<Configuration> assets;
	private final NamedDomainObjectProvider<Configuration> content;
	private final NamedDomainObjectProvider<Configuration> templates;
	private final NamedDomainObjectProvider<Configuration> properties;

	private final NamedDomainObjectProvider<Configuration> assetsElements;
	private final NamedDomainObjectProvider<Configuration> contentElements;
	private final NamedDomainObjectProvider<Configuration> templatesElements;
	private final NamedDomainObjectProvider<Configuration> propertiesElements;

	private final NamedDomainObjectProvider<Configuration> bakedElements;

	private final NamedDomainObjectProvider<Configuration> jbake;

	public JBakeDependencies(Project project, Names names) {
		final ConfigurationContainer configurations = project.getConfigurations();
		this.jbake = configurations.register(names.configurationName(JBAKE_CONFIGURATION_NAME),
			new AsDeclarable(new ConfigureJBakeExtensionDescription("JBake dependencies", it -> {})));

		this.assets = configurations.register(names.configurationName(ASSETS_CONFIGURATION_NAME),
			new AsResolvable(new ConfigureJBakeExtensionDescription("Assets", new JBakeAssetsConfiguration(project))));
		this.content = configurations.register(names.configurationName(CONTENT_CONFIGURATION_NAME),
			new AsResolvable(new ConfigureJBakeExtensionDescription("Content", new JBakeContentConfiguration(project))));
		this.templates = configurations.register(names.configurationName(TEMPLATES_CONFIGURATION_NAME),
			new AsResolvable(new ConfigureJBakeExtensionDescription("Templates", new JBakeTemplatesConfiguration(project))));
		this.properties = configurations.register(names.configurationName(CONFIGURATION_CONFIGURATION_NAME),
			new AsResolvable(new ConfigureJBakeExtensionDescription("Configuration", new JBakePropertiesConfiguration(project))));
		this.assetsElements = configurations.register(names.configurationName(ASSETS_ELEMENTS_CONFIGURATION_NAME),
			new AsConsumable(new ConfigureJBakeExtensionDescription("Assets elements", new JBakeAssetsConfiguration(project))));
		this.contentElements = configurations.register(names.configurationName(CONTENT_ELEMENTS_CONFIGURATION_NAME),
			new AsConsumable(new ConfigureJBakeExtensionDescription("Content elements", new JBakeContentConfiguration(project))));
		this.templatesElements = configurations.register(names.configurationName(TEMPLATES_ELEMENTS_CONFIGURATION_NAME),
			new AsConsumable(new ConfigureJBakeExtensionDescription("Templates elements", new JBakeTemplatesConfiguration(project))));
		this.propertiesElements = configurations.register(names.configurationName(CONFIGURATION_ELEMENTS_CONFIGURATION_NAME),
			new AsConsumable(new ConfigureJBakeExtensionDescription("Configuration elements", new JBakePropertiesConfiguration(project))));
		this.bakedElements = configurations.register(names.configurationName(BAKED_ELEMENTS_CONFIGURATION_NAME),
			new AsConsumable(new ExcludeFromAssembleTask(new ConfigureJBakeExtensionDescription("Baked elements", new JBakeBakedConfiguration(project)))));
	}

	public NamedDomainObjectProvider<Configuration> getAssets() {
		return assets;
	}

	public NamedDomainObjectProvider<Configuration> getContent() {
		return content;
	}

	public NamedDomainObjectProvider<Configuration> getTemplates() {
		return templates;
	}

	public NamedDomainObjectProvider<Configuration> getProperties() {
		return properties;
	}

	public NamedDomainObjectProvider<Configuration> getAssetsElements() {
		return assetsElements;
	}

	public NamedDomainObjectProvider<Configuration> getContentElements() {
		return contentElements;
	}

	public NamedDomainObjectProvider<Configuration> getTemplatesElements() {
		return templatesElements;
	}

	public NamedDomainObjectProvider<Configuration> getPropertiesElements() {
		return propertiesElements;
	}

	public NamedDomainObjectProvider<Configuration> getBakedElements() {
		return bakedElements;
	}

	public NamedDomainObjectProvider<Configuration> getJBake() {
		return jbake;
	}
}
