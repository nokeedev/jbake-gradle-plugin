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
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.bundling.Zip;

import java.util.Collections;

import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.DIRECTORY_TYPE;

final class FileCollectionArtifact implements Action<Configuration> {
	private final Project project;
	private final FileCollection files;

	public FileCollectionArtifact(Project project, FileCollection files) {
		this.project = project;
		this.files = files;
	}

	@Override
	public void execute(Configuration configuration) {
		final NamedDomainObjectProvider<Zip> zipTask = project.getTasks().register(zipTaskName(configuration), Zip.class);
		zipTask.configure((task) -> {
			task.getArchiveClassifier().set(guessClassifier(configuration.getName()));
			task.getDestinationDirectory().value(project.getLayout().getBuildDirectory().dir("tmp/" + task.getName())).disallowChanges();
		});
		zipTask.configure(task -> task.from(files));

		ListProperty<PublishArtifact> artifacts = project.getObjects().listProperty(PublishArtifact.class).value(project.provider(() -> {
			if (files.getAsFileTree().isEmpty()) {
				return Collections.emptyList();
			} else {
				return Collections.singletonList(new LazyPublishArtifact(zipTask));
			}
		}));
		artifacts.finalizeValueOnRead();
		artifacts.disallowChanges();
		configuration.getOutgoing().getArtifacts().addAllLater(artifacts);

		final NamedDomainObjectProvider<Sync> stageTask = project.getTasks().register(stageTaskName(configuration), Sync.class);
		stageTask.configure(task -> task.setDestinationDir(project.getLayout().getBuildDirectory().dir("tmp/" + task.getName()).get().getAsFile()));
		stageTask.configure(task -> task.from(files));
		configuration.getOutgoing().getVariants().create("directory", (variant) -> {
			variant.artifact(stageTask.map(Sync::getDestinationDir), it -> {
				it.setType(DIRECTORY_TYPE);
				it.builtBy(stageTask);
			});
		});
	}

	private static String zipTaskName(Configuration configuration) {
		return "zip" + capitalized(configuration.getName());
	}

	private static String stageTaskName(Configuration configuration) {
		return "stage" + capitalized(configuration.getName());
	}

	private static String guessClassifier(String configurationName) {
		return configurationName.replace("Elements", "");
	}

	private static String capitalized(String s) {
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}
}
