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
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;

public abstract class JBakeServeTask extends DefaultTask {
	private final ExecOperations execOperations;

	@Inject
	public JBakeServeTask(ExecOperations execOperations) {
		this.execOperations = execOperations;
	}

	@SkipWhenEmpty
	@InputDirectory
	public abstract DirectoryProperty getServeDirectory();

	@TaskAction
	private void doServe() {
		execOperations.exec(spec -> spec.commandLine("docker", "run", "--rm", "--mount", "type=bind,source=" + getServeDirectory().get().getAsFile().getAbsolutePath() + ",target=/usr/share/nginx/html", "--publish", "80:80", "nginx:alpine"));
	}
}
