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
import org.gradle.api.NamedDomainObjectCollectionSchema;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;
import java.io.File;
import java.util.Optional;

public abstract class JBakeExtension implements ExtensionAware {
	public static final String JBAKE_EXTENSION_NAME = "jbake";
	private final TaskProvider<Sync> stageTask;
	private final TaskProvider<JBakeTask> bakeTask;
	private final JBakeDependencies dependencies;
	private final Project project;
	private final Names names;

	@Inject
	public JBakeExtension(Project project, TaskContainer tasks, Names names) {
		this.project = project;
		this.names = names;
		this.stageTask = tasks.register(names.taskName("stageBake"), Sync.class);
		this.bakeTask = tasks.register(names.taskName("bake"), JBakeTask.class);
		this.dependencies = new JBakeDependencies(project, names);

		dependencies(new ExtendsFromJBakeConfiguration());
		dependencies(new AttachJBakeArtifacts(project, this, names));
		Optional.of(project.getGroup().toString())
			.filter(it -> !it.isEmpty())
			.flatMap(group -> names.capability().map(capability -> group + ":" + capability + ":" + project.getVersion()))
			.ifPresent(capability -> dependencies(new ConfigureJBakeExtensionOutgoingCapability(capability)));
		getStageTask().configure(new JBakeStageTask(project, this));
		getBakeTask().configure(new JBakeBakeTask(project, this));
		getDestinationDirectory().value(getBakeTask().flatMap(JBakeTask::getDestinationDirectory)).disallowChanges();
	}

	public abstract ConfigurableFileCollection getClasspath();

	public JBakeContentExtension getContent() {
		return getExtensions().getByType(JBakeContentExtension.class);
	}

	public void content(Action<? super CopySpec> action) {
		getContent().configure(action);
	}

	public JBakeAssetsExtension getAssets() {
		return getExtensions().getByType(JBakeAssetsExtension.class);
	}

	public void assets(Action<? super CopySpec> action) {
		getAssets().configure(action);
	}

	public JBakeTemplatesExtension getTemplates() {
		return getExtensions().getByType(JBakeTemplatesExtension.class);
	}

	public void templates(Action<? super CopySpec> action) {
		getTemplates().configure(action);
	}

	public JBakePropertiesExtension getConfigurations() {
		return getExtensions().getByType(JBakePropertiesExtension.class);
	}

	public void configurations(Action<? super MapProperty<String, Object>> action) {
		getConfigurations().configure(action);
	}

	public abstract DirectoryProperty getDestinationDirectory();

	public TaskProvider<Sync> getStageTask() {
		return stageTask;
	}

	public TaskProvider<JBakeTask> getBakeTask() {
		return bakeTask;
	}

	public JBakeDependencies getDependencies() {
		return dependencies;
	}

	public void dependencies(Action<? super JBakeDependencies> action) {
		action.execute(dependencies);
	}

	public static void jbake(ExtensionAware target, Action<? super JBakeExtension> action) {
		action.execute((JBakeExtension) target.getExtensions().getByName(JBAKE_EXTENSION_NAME));
	}

	public static JBakeExtension jbake(ExtensionAware target) {
		return (JBakeExtension) target.getExtensions().getByName(JBAKE_EXTENSION_NAME);
	}
}
