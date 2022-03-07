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
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.capabilities.Capability;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static net.nokeedev.jbake.ProviderMatcher.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertAll;

class JBakePluginIntegrationTest {
	private final Project project = ProjectBuilder.builder().build();

	@BeforeEach
	void applyPlugin() {
		project.getPluginManager().apply("net.nokeedev.jbake-site");
	}

	@Test
	void registerJBakeExtensionOnProject() {
		assertThat(project.getExtensions().findByName("jbake"), isA(JBakeExtension.class));
	}

	@Test
	void hasDefaultCapabilityOnly() {
		final JBakeDependencies subject = project.getExtensions().getByType(JBakeExtension.class).getDependencies();
		final Transformer<Iterable<? extends Capability>, Configuration> toCapabilities = it -> it.getOutgoing().getCapabilities();
		assertAll(
			() -> assertThat(subject.getBakedElements().map(toCapabilities), providerOf(emptyIterable())),
			() -> assertThat(subject.getAssetsElements().map(toCapabilities), providerOf(emptyIterable())),
			() -> assertThat(subject.getContentElements().map(toCapabilities), providerOf(emptyIterable())),
			() -> assertThat(subject.getTemplatesElements().map(toCapabilities), providerOf(emptyIterable())),
			() -> assertThat(subject.getPropertiesElements().map(toCapabilities), providerOf(emptyIterable()))
		);
	}
}
