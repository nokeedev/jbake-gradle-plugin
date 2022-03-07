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

import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.Sync;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static net.nokeedev.jbake.NamedMatcher.named;
import static net.nokeedev.jbake.ProviderMatcher.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.isA;

class JBakeExtensionIntegrationTest {
	private final Project project = ProjectBuilder.builder().build();
	private final JBakeExtension subject = JBakeExtensionFactory.forProject(project).create("hjei");

	@Test
	void hasClasspath() {
		assertThat(subject.getClasspath(), isA(ConfigurableFileCollection.class));
	}

	@Test
	void hasAssets() {
		assertThat(subject.getAssets(), isA(ConfigurableFileCollection.class));
	}

	@Test
	void hasContent() {
		assertThat(subject.getContent(), isA(ConfigurableFileCollection.class));
	}

	@Test
	void hasTemplates() {
		assertThat(subject.getTemplates(), isA(ConfigurableFileCollection.class));
	}

	@Test
	void hasConfigurations() {
		assertThat(subject.getConfigurations(), isA(MapProperty.class));
	}

	@Test
	void hasDestinationDirectory() {
		assertThat(subject.getDestinationDirectory(), isA(DirectoryProperty.class));
	}

	@Test
	void hasDependencies() {
		assertThat(subject.getDependencies(), isA(JBakeDependencies.class));
	}

	@Test
	void hasStageTask() {
		assertThat(subject.getStageTask(), providerOf(allOf(named("stageBakeHjei"), isA(Sync.class))));
	}

	@Test
	void hasBakeTask() {
		assertThat(subject.getBakeTask(), providerOf(allOf(named("bakeHjei"), isA(JBakeTask.class))));
	}
}
