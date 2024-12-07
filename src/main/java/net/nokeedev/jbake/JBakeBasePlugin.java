package net.nokeedev.jbake;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.ArchiveOperations;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.provider.Provider;
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
		project.getPlugins().withType(PublishingPlugin.class, ignored -> jbake(project, new RegisterJBakeComponent(project)));

		project.getDependencies().registerTransform(UnzipJBakeArchiveTransform.class, spec -> {
			spec.getFrom().attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "jbake-archive");
			spec.getFrom().attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "jbake"));

			spec.getTo().attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "jbake-directory");
			spec.getTo().attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "jbake"));
		});
	}

	/*private*/ static abstract /*final*/ class UnzipJBakeArchiveTransform implements TransformAction<TransformParameters.None> {
		private final ArchiveOperations archiveOperations;
		private final FileSystemOperations fileOperations;

		@Inject
		public UnzipJBakeArchiveTransform(ArchiveOperations archiveOperations, FileSystemOperations fileOperations) {
			this.archiveOperations = archiveOperations;
			this.fileOperations = fileOperations;
		}

		@InputArtifact
		protected abstract Provider<FileSystemLocation> getInputArtifact();

		@Override
		public void transform(TransformOutputs outputs) {
			fileOperations.sync(spec -> {
				spec.from(archiveOperations.zipTree(getInputArtifact()));
				spec.into(outputs.dir("out"));
			});
		}
	}
}
