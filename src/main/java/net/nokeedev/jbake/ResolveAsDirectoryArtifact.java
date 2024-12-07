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
import org.gradle.api.artifacts.ArtifactView;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Attribute;

import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.DIRECTORY_TYPE;

final class ResolveAsDirectoryArtifact implements Action<ArtifactView.ViewConfiguration> {
	private static final Attribute<String> ARTIFACT_TYPE_ATTRIBUTE = Attribute.of("artifactType", String.class);
	private final String artifactType;

	public ResolveAsDirectoryArtifact(String artifactType) {
		this.artifactType = artifactType;
	}

	@Override
	public void execute(ArtifactView.ViewConfiguration view) {
		view.attributes(it -> it.attribute(ARTIFACT_TYPE_ATTRIBUTE, artifactType));
	}
}
