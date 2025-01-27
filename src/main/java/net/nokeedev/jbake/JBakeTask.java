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

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.*;
import org.gradle.execution.MultipleBuildFailures;
import org.gradle.workers.ProcessWorkerSpec;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkerExecutor;
import org.jbake.app.Oven;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.app.configuration.JBakeConfigurationFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

@SuppressWarnings("UnstableApiUsage")
public abstract class JBakeTask extends DefaultTask {
	private final WorkerExecutor workers;
	private final FileSystemOperations fileOperations;

	@InputDirectory
	public abstract DirectoryProperty getSourceDirectory();

	@SkipWhenEmpty
	@IgnoreEmptyDirectories
	@InputFiles
	public FileCollection getSources() {
		return getSourceDirectory().dir("content").get().getAsFileTree();
	}

	@Input
	public abstract MapProperty<String, Object> getConfigurations();

	@Classpath
	public abstract ConfigurableFileCollection getClasspath();

	@OutputDirectory
	public abstract DirectoryProperty getDestinationDirectory();

	@Inject
	public JBakeTask(WorkerExecutor workers, FileSystemOperations fileOperations) {
		this.workers = workers;
		this.fileOperations = fileOperations;
	}

	@TaskAction
	private void doRender() {
		fileOperations.delete(spec -> spec.delete(getDestinationDirectory()));
		workers.processIsolation(this::configureClasspath).submit(JBakeTaskWorkAction.class, this::configureAction);
	}

	private void configureClasspath(ProcessWorkerSpec spec) {
		spec.getClasspath().from(getClasspath());
	}

	private void configureAction(JBakeTaskWorkAction.Parameters parameters) {
		parameters.getSourceDirectory().set(getSourceDirectory());
		parameters.getDestinationDirectory().set(getDestinationDirectory());
		parameters.getConfigurations().set(getConfigurations());
	}

	@SuppressWarnings("UnstableApiUsage")
	/*private*/ abstract static /*final*/ class JBakeTaskWorkAction implements WorkAction<JBakeTaskWorkAction.Parameters> {
		public interface Parameters extends WorkParameters {
			DirectoryProperty getSourceDirectory();
			DirectoryProperty getDestinationDirectory();
			MapProperty<String, Object> getConfigurations();
		}

		private static final Logger LOGGER = Logging.getLogger(JBakeTaskWorkAction.class);

		@Inject
		public JBakeTaskWorkAction() {}

		@Override
		public void execute() {
			final Oven jbake = new Oven(jbakeConfiguration(getParameters()));
			jbake.bake();
			final List<Throwable> errors = jbake.getErrors();
			if (!errors.isEmpty()) {
				errors.forEach(it -> LOGGER.error(it.getMessage()));
				throw new IllegalStateException(new MultipleBuildFailures(errors));
			}
		}

		private static JBakeConfiguration jbakeConfiguration(Parameters parameters) {
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

		private static File configuration(Parameters parameters) {
			Properties properties = new Properties();
			parameters.getConfigurations().get().forEach((k, v) -> {
				properties.put(k, v.toString());
			});

			try {
				final Path result = Files.createTempFile("jbake", ".properties");
				try (OutputStream outStream = Files.newOutputStream(result)) {
					properties.store(outStream, null);
				}

				return result.toFile();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
}
