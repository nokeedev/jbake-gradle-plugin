package net.nokeedev.jbake;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/*private*/ abstract /*final*/ class JBakeBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getExtensions().add(JBakeExtension.JBAKE_EXTENSION_NAME, JBakeExtensionFactory.forProject(project).create());
	}
}
