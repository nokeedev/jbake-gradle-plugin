package net.nokeedev.jbake;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.plugins.PublishingPlugin;

import javax.inject.Inject;

import static net.nokeedev.jbake.JBakeExtension.jbake;

/*private*/ abstract /*final*/ class JBakeBasePlugin implements Plugin<Project> {
	@Inject
	public JBakeBasePlugin() {}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("lifecycle-base");

		project.getExtensions().add(JBakeExtension.JBAKE_EXTENSION_NAME, JBakeExtensionFactory.forProject(project).create());
		new RegisterJBakeArtifactTransforms(project).execute(project.getDependencies());
		project.getPlugins().withType(PublishingPlugin.class, ignored -> jbake(project, new RegisterJBakeComponent(project)));
	}
}
