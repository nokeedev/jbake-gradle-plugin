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
import org.gradle.api.artifacts.Configuration;

// See https://github.com/gradle/gradle/issues/6875#issuecomment-1084140718
final class ExcludeFromAssembleTask implements Action<Configuration> {
	private final Action<Configuration> delegate;

	ExcludeFromAssembleTask(Action<Configuration> delegate) {
		this.delegate = delegate;
	}

	@Override
	public void execute(Configuration configuration) {
		delegate.execute(configuration);
		configuration.setVisible(false);
	}
}
