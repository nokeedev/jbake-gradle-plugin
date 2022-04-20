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

public abstract class JBakeExtension {
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
		dependencies(new ResolveAsDirectoryArtifact());
		Optional.of(project.getGroup().toString())
			.filter(it -> !it.isEmpty())
			.flatMap(group -> names.capability().map(capability -> group + ":" + capability + ":" + project.getVersion()))
			.ifPresent(capability -> dependencies(new ConfigureJBakeExtensionOutgoingCapability(capability)));
		getStageTask().configure(new JBakeStageTask(project, this));
		getBakeTask().configure(new JBakeBakeTask(project, this));
		getDestinationDirectory().convention(getBakeTask().flatMap(JBakeTask::getDestinationDirectory));
	}

	public abstract ConfigurableFileCollection getClasspath();
	public abstract ConfigurableFileCollection getContent();
	public Provider<File> sync(String name, Action<? super CopySpec> action) {
		final String syncTaskName = names.taskName("sync", name);
		for (NamedDomainObjectCollectionSchema.NamedDomainObjectSchema element : project.getTasks().getCollectionSchema().getElements()) {
			if (element.getName().equals(syncTaskName)) {
				return project.getTasks().named(syncTaskName, Sync.class, action).map(Sync::getDestinationDir);
			}
		}

		final TaskProvider<Sync> syncTask = project.getTasks().register(syncTaskName, Sync.class, task -> {
			task.setIncludeEmptyDirs(false);
			task.setDestinationDir(project.getLayout().getBuildDirectory().dir("tmp/" + task.getName()).get().getAsFile());
		});
		syncTask.configure(action);
		return syncTask.map(Sync::getDestinationDir);
	}

	public abstract ConfigurableFileCollection getAssets();
	public abstract ConfigurableFileCollection getTemplates();

	@SuppressWarnings("UnstableApiUsage")
	public abstract MapProperty<String, Object> getConfigurations();

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
