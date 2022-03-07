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

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.MapConfiguration;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.MapProperty;
import org.gradle.execution.MultipleBuildFailures;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.jbake.app.Oven;
import org.jbake.app.configuration.ConfigUtil;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.app.configuration.JBakeConfigurationFactory;

import javax.inject.Inject;
import java.io.File;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
abstract class JBakeTaskWorkAction implements WorkAction<JBakeTaskWorkAction.Parameters> {
	private static final Logger LOGGER = Logging.getLogger(JBakeTaskWorkAction.class);

	@Inject
	public JBakeTaskWorkAction() {}

	@Override
	public void execute() {
		try {
			final Oven jbake = new Oven(jbakeConfiguration(getParameters()));
			jbake.bake();
			final List<Throwable> errors = jbake.getErrors();
			if (!errors.isEmpty()) {
				errors.forEach(it -> LOGGER.error(it.getMessage()));
				throw new IllegalStateException(new MultipleBuildFailures(errors));
			}
		} catch (ConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	private static JBakeConfiguration jbakeConfiguration(Parameters parameters) throws ConfigurationException {
		final JBakeConfigurationFactory factory = new JBakeConfigurationFactory();
		return factory.createDefaultJbakeConfiguration(
			sourceDirectory(parameters),
			destinationDirectory(parameters),
			configuration(parameters),
			false);
	}

	private static File sourceDirectory(Parameters parameters) {
		return parameters.getSourceDirectory().get().getAsFile();
	}

	private static File destinationDirectory(Parameters parameters) {
		return parameters.getDestinationDirectory().get().getAsFile();
	}

	private static CompositeConfiguration configuration(Parameters parameters) throws ConfigurationException {
		final CompositeConfiguration result = new CompositeConfiguration();
		result.addConfiguration(new MapConfiguration(new HashMap<>(parameters.getConfigurations().get())));
		result.addConfiguration(defaultConfiguration(parameters));
		return result;
	}

	private static Configuration defaultConfiguration(Parameters parameters) throws ConfigurationException {
		return ((DefaultJBakeConfiguration) new ConfigUtil().loadConfig(parameters.getSourceDirectory().get().getAsFile())).getCompositeConfiguration();
	}

	public interface Parameters extends WorkParameters {
		DirectoryProperty getSourceDirectory();
		DirectoryProperty getDestinationDirectory();
		MapProperty<String, Object> getConfigurations();
	}
}
