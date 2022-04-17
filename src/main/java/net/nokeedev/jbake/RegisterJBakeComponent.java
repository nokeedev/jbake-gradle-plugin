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
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.component.ConfigurationVariantDetails;

final class RegisterJBakeComponent implements Action<JBakeExtension> {
	private final Project project;
	private final AdhocSoftwareComponentFactory factory;

	RegisterJBakeComponent(Project project) {
		this.project = project;
		this.factory = AdhocSoftwareComponentFactory.forProject(project);
	}

	@Override
	public void execute(JBakeExtension extension) {
		final AdhocComponentWithVariants component = factory.create("jbake");
		final Action<ConfigurationVariantDetails> skipIfOnAnyUnpublishableArtifactType = new SkipIf(new HasUnpublishableArtifactType());

		component.addVariantsFromConfiguration(
			extension.getDependencies().getAssetsElements().get(),
			skipIfOnAnyUnpublishableArtifactType);
		component.addVariantsFromConfiguration(
			extension.getDependencies().getTemplatesElements().get(),
			skipIfOnAnyUnpublishableArtifactType);
		component.addVariantsFromConfiguration(
			extension.getDependencies().getContentElements().get(),
			skipIfOnAnyUnpublishableArtifactType);
		component.addVariantsFromConfiguration(
			extension.getDependencies().getPropertiesElements().get(),
			skipIfOnAnyUnpublishableArtifactType);
		component.addVariantsFromConfiguration(
			extension.getDependencies().getBakedElements().get(),
			skipIfOnAnyUnpublishableArtifactType);

		project.getComponents().add(component);
	}
}
