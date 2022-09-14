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
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.io.FileMatchers.aFileWithAbsolutePath;

class JBakeExtensionDestinationDirectoryIntegrationTest {
	private final Project project = ProjectBuilder.builder().build();
	private final JBakeExtension subject = JBakeExtensionFactory.forProject(project).create("keol");

	@Test
	void isConnectedWithBakeTask() {
		subject.getBakeTask().get().getDestinationDirectory().set(project.getLayout().getProjectDirectory().dir("out"));
		assertThat(subject.getDestinationDirectory().get().getAsFile(), aFileWithAbsolutePath(endsWith("/out")));
	}
}
