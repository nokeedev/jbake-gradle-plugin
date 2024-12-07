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
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.artifacts.transform.TransformSpec;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.Usage;
import org.gradle.api.internal.artifacts.transform.UnzipTransform;

import static net.nokeedev.jbake.JBakeAssetsConfiguration.JBAKE_ASSETS_USAGE_NAME;
import static net.nokeedev.jbake.JBakeBakedConfiguration.JBAKE_BAKED_USAGE_NAME;
import static net.nokeedev.jbake.JBakeContentConfiguration.JBAKE_CONTENT_USAGE_NAME;
import static net.nokeedev.jbake.JBakeTemplatesConfiguration.JBAKE_TEMPLATES_USAGE_NAME;
import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.DIRECTORY_TYPE;
import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.ZIP_TYPE;

final class RegisterJBakeArtifactTransforms implements Action<DependencyHandler> {
	private final Project project;

	RegisterJBakeArtifactTransforms(Project project) {
		this.project = project;
	}

	@Override
	public void execute(DependencyHandler dependencies) {
		dependencies.registerTransform(UnzipTransform.class,
			unzipArtifact(project.getObjects().named(Usage.class, JBAKE_CONTENT_USAGE_NAME)));
		dependencies.registerTransform(UnzipTransform.class,
			unzipArtifact(project.getObjects().named(Usage.class, JBAKE_ASSETS_USAGE_NAME)));
		dependencies.registerTransform(UnzipTransform.class,
			unzipArtifact(project.getObjects().named(Usage.class, JBAKE_TEMPLATES_USAGE_NAME)));
		dependencies.registerTransform(UnzipTransform.class,
			unzipArtifact(project.getObjects().named(Usage.class, JBAKE_BAKED_USAGE_NAME)));
	}

	private static final Attribute<String> ARTIFACT_FORMAT = Attribute.of("artifactType", String.class);
	private static Action<TransformSpec<TransformParameters.None>> unzipArtifact(Usage targetUsage) {
		return variantTransform -> {
			variantTransform.getFrom().attribute(ARTIFACT_FORMAT, ZIP_TYPE);
			variantTransform.getFrom().attribute(Usage.USAGE_ATTRIBUTE, targetUsage);
			variantTransform.getTo().attribute(ARTIFACT_FORMAT, targetUsage.getName() + "-directory");
			variantTransform.getTo().attribute(Usage.USAGE_ATTRIBUTE, targetUsage);
		};
	}
}
