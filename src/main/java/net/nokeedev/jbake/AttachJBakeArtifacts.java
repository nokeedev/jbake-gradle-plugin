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
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;

final class AttachJBakeArtifacts implements Action<JBakeDependencies> {
	private final Project project;
	private final JBakeExtension extension;
	private final Names names;

	AttachJBakeArtifacts(Project project, JBakeExtension extension, Names names) {
		this.project = project;
		this.extension = extension;
		this.names = names;
	}

	@Override
	public void execute(JBakeDependencies dependencies) {
		dependencies.getAssetsElements().configure(new FileCollectionArtifact(project, extension.getAssets()));
		dependencies.getContentElements().configure(new FileCollectionArtifact(project, extension.getContent()));
		dependencies.getTemplatesElements().configure(new FileCollectionArtifact(project, extension.getTemplates()));
		dependencies.getBakedElements().configure(new DirectoryArtifact(project, extension.getDestinationDirectory()));
		dependencies.getPropertiesElements().configure(artifactIfExists(jbakeProperties().map(RegularFile::getAsFile)));
	}

	private Provider<RegularFile> jbakeProperties() {
		final TaskProvider<GenerateJBakeProperties> bakePropertiesTask = project.getTasks().register(names.taskName("bakeProperties"), GenerateJBakeProperties.class, task -> {
			task.getConfigurations().value(extension.getConfigurations()).disallowChanges();
			task.getOutputFile().value(project.getLayout().getBuildDirectory().file("tmp/" + task.getName() + "/jbake.properties"));
		});
		// Because publish artifacts gets queried early, we can't flat map the output property.
		//   Instead, we map the task to the property value... it's a workaround to achieve the same thing:
		//   aka. a provider to the output file with implicit task dependency
		return bakePropertiesTask.map(it -> it.getOutputFile().get());
	}

	private Action<Configuration> artifactIfExists(Provider<File> fileProvider) {
		return configuration -> {
			configuration.getOutgoing().artifact(fileProvider);
		};
	}
}
