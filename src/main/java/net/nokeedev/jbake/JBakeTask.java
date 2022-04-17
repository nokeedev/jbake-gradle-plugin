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
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.*;
import org.gradle.workers.ProcessWorkerSpec;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;

@SuppressWarnings("UnstableApiUsage")
public abstract class JBakeTask extends DefaultTask {
	private final WorkerExecutor workers;
	private final FileSystemOperations fileOperations;

	@InputDirectory
	public abstract DirectoryProperty getSourceDirectory();

	@SkipWhenEmpty
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

}
