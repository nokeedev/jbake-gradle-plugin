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
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Sync;

@SuppressWarnings("UnstableApiUsage")
final class JBakeBakeTask implements Action<JBakeTask> {
	private final Project project;
	private final JBakeExtension extension;
	private final ConfigurationContainer configurations;
	private final DependencyHandler dependencies;

	public JBakeBakeTask(Project project, JBakeExtension extension) {
		this.project = project;
		this.extension = extension;
		this.configurations = project.getConfigurations();
		this.dependencies = project.getDependencies();
	}

	@Override
	public void execute(JBakeTask task) {
			task.setGroup("documentation");
			task.setDescription("Bakes with JBake");
			task.getSourceDirectory().fileProvider(extension.getStageTask().map(Sync::getDestinationDir));
			task.getDestinationDirectory().value(project.getLayout().getBuildDirectory().dir("jbake"));
//			task.getConfigurations().putAll(extension.getConfigurations().getAllElements());
			task.getConfigurations().put("working.directory", extension.getStageTask().map(this::relativeToProjectDirectory));
			task.getClasspath()
				.from(jbake("2.6.7"))
				.from(asciidoctor("2.4.3"))
				.from(groovyTemplates("3.0.7"))
				.from(flexmarkTemplates("0.62.2"))
				.from(freemarkerTemplates("2.3.31"))
				.from(pegdownTemplates("1.6.0"))
				.from(thymeleafTemplates("3.0.12.RELEASE"))
				.from(jade4jTemplates("1.3.2"))
				.from(pebbleTemplates("3.1.5"))
			;
	}

	private String relativeToProjectDirectory(Sync task) {
		return project.getLayout().getProjectDirectory().getAsFile().toPath().relativize(task.getDestinationDir().toPath()).toString();
	}

	private FileCollection jbake(String version) {
		return configurations.detachedConfiguration(dependencies.create("org.jbake:jbake-core:" + version));
	}

	// TODO: Move to asciidoctor-language plugin
	private FileCollection asciidoctor(String version) {
		return configurations.detachedConfiguration(dependencies.create("org.asciidoctor:asciidoctorj:" + version));
	}

	// TODO: Move to groovy-templates plugin?
	private FileCollection groovyTemplates(String version) {
		return configurations.detachedConfiguration(dependencies.create("org.codehaus.groovy:groovy:" + version), dependencies.create("org.codehaus.groovy:groovy-templates:" + version));
	}

	// TODO: Move to flexmark-templates plugin?
	private FileCollection flexmarkTemplates(String version) {
		return configurations.detachedConfiguration(dependencies.create("com.vladsch.flexmark:flexmark:" + version), dependencies.create("com.vladsch.flexmark:flexmark-profile-pegdown:" + version));
	}

	// TODO: Move to freemarker-templates plugin?
	private FileCollection freemarkerTemplates(String version) {
		return configurations.detachedConfiguration(dependencies.create("org.freemarker:freemarker:" + version));
	}

	// TODO: Move to pegdown-templates plugin?
	private FileCollection pegdownTemplates(String version) {
		return configurations.detachedConfiguration(dependencies.create("org.pegdown:pegdown:" + version));
	}

	// TODO: Move to thymeleaf-templates plugin?
	private FileCollection thymeleafTemplates(String version) {
		return configurations.detachedConfiguration(dependencies.create("org.thymeleaf:thymeleaf:" + version));
	}

	// TODO: Move to jade4j-templates plugin?
	private FileCollection jade4jTemplates(String version) {
		return configurations.detachedConfiguration(dependencies.create("de.neuland-bfi:jade4j:" + version));
	}

	// TODO: Move to pebble-templates plugin?
	private FileCollection pebbleTemplates(String version) {
		return configurations.detachedConfiguration(dependencies.create("io.pebbletemplates:pebble:" + version));
	}
}
