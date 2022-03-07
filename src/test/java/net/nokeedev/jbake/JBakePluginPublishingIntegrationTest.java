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
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

class JBakePluginPublishingIntegrationTest {
	private final Project project = ProjectBuilder.builder().build();

	@BeforeEach
	void applyPlugin() {
		project.getPluginManager().apply("net.nokeedev.jbake-site");
	}

	@Nested
	class MavenPublishTest {
		@BeforeEach
		void applyPlugin() {
			project.getPluginManager().apply("maven-publish");
		}

		@Test
		void registerJBakeComponentOnProject() {
			assertThat(project.getComponents().findByName("jbake"), isA(AdhocComponentWithVariants.class));
		}
	}

	@Nested
	class IvyPublishTest {
		@BeforeEach
		void applyPlugin() {
			project.getPluginManager().apply("ivy-publish");
		}

		@Test
		void registerJBakeComponentOnProject() {
			assertThat(project.getComponents().findByName("jbake"), isA(AdhocComponentWithVariants.class));
		}
	}
}
