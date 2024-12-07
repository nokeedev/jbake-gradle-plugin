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
import org.gradle.api.artifacts.transform.*;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.internal.artifacts.transform.UnzipTransform;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;
import java.io.File;

import static net.nokeedev.jbake.JBakeAssetsConfiguration.JBAKE_ASSETS_USAGE_NAME;
import static net.nokeedev.jbake.JBakeBakedConfiguration.JBAKE_BAKED_USAGE_NAME;
import static net.nokeedev.jbake.JBakeContentConfiguration.JBAKE_CONTENT_USAGE_NAME;
import static net.nokeedev.jbake.JBakeTemplatesConfiguration.JBAKE_TEMPLATES_USAGE_NAME;
import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.DIRECTORY_TYPE;
import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.ZIP_TYPE;

final class RegisterJBakeArtifactTransforms implements Action<DependencyHandler> {
	private final Project project;
	private final ObjectFactory objects;

	RegisterJBakeArtifactTransforms(Project project) {
		this.project = project;
		this.objects = project.getObjects();
	}

	@Override
	public void execute(DependencyHandler dependencies) {
		dependencies.registerTransform(UnzipTransform.class,
			unzipArtifact(project.getObjects().named(Usage.class, "jbake")));

		dependencies.registerTransform(UnpackJBakeContentTransform.class, spec -> {
			spec.getFrom()
				.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, "jbake"))
				.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "jbake-directory")
				;
			spec.getTo()
				.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, "jbake"))
				.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "jbake-content-directory")
				;
		});

		dependencies.registerTransform(UnpackJBakeAssetsTransform.class, spec -> {
			spec.getFrom()
				.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, "jbake"))
				.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "jbake-directory")
			;
			spec.getTo()
				.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, "jbake"))
				.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "jbake-assets-directory")
			;
		});

		dependencies.registerTransform(UnpackJBakeTemplatesTransform.class, spec -> {
			spec.getFrom()
				.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, "jbake"))
				.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "jbake-directory")
			;
			spec.getTo()
				.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, "jbake"))
				.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "jbake-templates-directory")
			;
		});

		dependencies.registerTransform(UnpackJBakePropertiesTransform.class, spec -> {
			spec.getFrom()
				.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, "jbake"))
				.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "jbake-directory")
			;
			spec.getTo()
				.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, "jbake"))
				.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "jbake-properties")
			;
		});
	}

	private static final Attribute<String> ARTIFACT_FORMAT = Attribute.of("artifactType", String.class);
	private static Action<TransformSpec<TransformParameters.None>> unzipArtifact(Usage targetUsage) {
		return variantTransform -> {
			variantTransform.getFrom().attribute(ARTIFACT_FORMAT, "jbake-archive");
			variantTransform.getFrom().attribute(Usage.USAGE_ATTRIBUTE, targetUsage);
			variantTransform.getTo().attribute(ARTIFACT_FORMAT, "jbake-directory");
			variantTransform.getTo().attribute(Usage.USAGE_ATTRIBUTE, targetUsage);
		};
	}

	/*private*/ static abstract /*final*/ class UnpackJBakeContentTransform implements TransformAction<TransformParameters.None> {
		@Inject
		public UnpackJBakeContentTransform() {}

		@InputArtifact
		public abstract Provider<FileSystemLocation> getInputArtifact();

		@Override
		public void transform(TransformOutputs outputs) {
			File artifact = getInputArtifact().get().getAsFile();
			File contentDir = new File(artifact, "content");
			if (contentDir.exists()) {
				outputs.dir(contentDir);
			}
		}
	}

	/*private*/ static abstract /*final*/ class UnpackJBakeAssetsTransform implements TransformAction<TransformParameters.None> {
		@Inject
		public UnpackJBakeAssetsTransform() {}

		@InputArtifact
		public abstract Provider<FileSystemLocation> getInputArtifact();

		@Override
		public void transform(TransformOutputs outputs) {
			File artifact = getInputArtifact().get().getAsFile();
			File contentDir = new File(artifact, "assets");
			if (contentDir.exists()) {
				outputs.dir(contentDir);
			}
		}
	}

	/*private*/ static abstract /*final*/ class UnpackJBakeTemplatesTransform implements TransformAction<TransformParameters.None> {
		@Inject
		public UnpackJBakeTemplatesTransform() {}

		@InputArtifact
		public abstract Provider<FileSystemLocation> getInputArtifact();

		@Override
		public void transform(TransformOutputs outputs) {
			File artifact = getInputArtifact().get().getAsFile();
			File contentDir = new File(artifact, "templates");
			if (contentDir.exists()) {
				outputs.dir(contentDir);
			}
		}
	}

	/*private*/ static abstract /*final*/ class UnpackJBakePropertiesTransform implements TransformAction<TransformParameters.None> {
		@Inject
		public UnpackJBakePropertiesTransform() {}

		@InputArtifact
		public abstract Provider<FileSystemLocation> getInputArtifact();

		@Override
		public void transform(TransformOutputs outputs) {
			File artifact = getInputArtifact().get().getAsFile();
			File file = new File(artifact, "jbake.properties");
			if (file.exists()) {
				outputs.file(file);
			}
		}
	}
}
