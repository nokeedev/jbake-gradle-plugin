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
import org.gradle.api.artifacts.ConfigurationVariant;
import org.gradle.api.component.ConfigurationVariantDetails;
import org.gradle.api.specs.Spec;

final class SkipIf implements Action<ConfigurationVariantDetails> {
	private final Spec<? super ConfigurationVariant> spec;

	public SkipIf(Spec<? super ConfigurationVariant> spec) {
		this.spec = spec;
	}

	@Override
	public void execute(ConfigurationVariantDetails variantDetails) {
		if (spec.isSatisfiedBy(variantDetails.getConfigurationVariant())) {
			variantDetails.skip();
		}
	}
}
