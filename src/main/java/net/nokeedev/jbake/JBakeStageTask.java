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
import org.gradle.api.Task;
import org.gradle.api.tasks.Sync;

import java.io.File;

final class JBakeStageTask implements Action<Sync> {
	private final Project project;
	private final JBakeExtension extension;

	JBakeStageTask(Project project, JBakeExtension extension) {
		this.project = project;
		this.extension = extension;
	}

	@Override
	public void execute(Sync task) {
		task.doLast(new AvoidMissingJBakeDirectoryWarnings());
		task.setDestinationDir(project.getLayout().getBuildDirectory().dir("tmp/" + task.getName()).get().getAsFile());
		task.setIncludeEmptyDirs(false);
	}

	private static final class AvoidMissingJBakeDirectoryWarnings implements Action<Task> {
		@Override
		public void execute(Task task) {
			new File(((Sync) task).getDestinationDir(), "content").mkdirs();
			new File(((Sync) task).getDestinationDir(), "assets").mkdirs();
			new File(((Sync) task).getDestinationDir(), "templates").mkdirs();
		}
	}
}
