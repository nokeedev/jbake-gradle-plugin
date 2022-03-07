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
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.bundling.Zip;

import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.DIRECTORY_TYPE;

final class DirectoryArtifact implements Action<Configuration> {
	private final Project project;
	private final Provider<Directory> directoryProvider;

	DirectoryArtifact(Project project, Provider<Directory> directoryProvider) {
		this.project = project;
		this.directoryProvider = directoryProvider;
	}

	@Override
	public void execute(Configuration configuration) {
		final NamedDomainObjectProvider<Zip> zipTask = project.getTasks().register(zipTaskName(configuration), Zip.class);
		zipTask.configure(task -> {
			task.getArchiveClassifier().set(guessClassifier(configuration.getName()));
			task.getDestinationDirectory().value(project.getLayout().getBuildDirectory().dir("tmp/" + task.getName())).disallowChanges();
		});
		zipTask.configure(task -> task.from(directoryProvider));
		configuration.getOutgoing().artifact(zipTask);
		configuration.getOutgoing().getVariants().maybeCreate("directory").artifact(this.directoryProvider, it -> {
			it.setType(DIRECTORY_TYPE);
		});
	}

	private static String zipTaskName(Configuration configuration) {
		return "zip" + capitalized(configuration.getName());
	}

	private static String guessClassifier(String configurationName) {
		return configurationName.replace("Elements", "");
	}

	private static String capitalized(String s) {
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}
}
