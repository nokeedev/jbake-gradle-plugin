package net.nokeedev.jbake;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.*;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;
import java.io.File;
import java.util.concurrent.Callable;

public abstract /*final*/ class JBakeContentExtension implements JBakeContent {
	private final TaskProvider<Sync> assembleTask;
	private final ConfigurableFileCollection files;
	private final NamedDomainObjectProvider<Configuration> content;

	@Inject
	public JBakeContentExtension(Names names, ObjectFactory objects, ConfigurationContainer configurations, TaskContainer tasks) {
		this.assembleTask = tasks.register(names.taskName("assemble", "JBaseContent"), Sync.class);
		this.content = configurations.register(names.configurationName("content"));

		content.configure(new AsDeclarable(it -> {}));
		content.configure(new ConfigureJBakeExtensionDescription("Content", it -> {}));

		this.assembleTask.configure(task -> {
			task.setDestinationDir(task.getTemporaryDir());
		});

		NamedDomainObjectProvider<Configuration> incomingContent = configurations.register(names.configurationName("jbakeContent"));
		incomingContent.configure(new AsResolvable(it -> {}));
		incomingContent.configure(it -> it.extendsFrom(content.get()));
		incomingContent.configure(new JBakeContentConfiguration(objects));
		incomingContent.configure(new ConfigureJBakeExtensionDescription("Content", it -> {}));

		this.files = objects.fileCollection().from(assembleTask)
			.from(incomingContent.get().getIncoming().artifactView(new ResolveAsDirectoryArtifact("jbake-content-directory")).getFiles());

		getLocation().fileProvider(assembleTask.map(Sync::getDestinationDir));
		getLocation().disallowChanges();
	}

	@Override
	public FileTree getAsFileTree() {
		return files.getAsFileTree();
	}

	public void configure(Action<? super CopySpec> action) {
		assembleTask.configure(action);
	}

	@Override
	public abstract DirectoryProperty getLocation();

	private Configuration asConfiguration() {
		return content.get();
	}

	/*private*/ static abstract /*final*/ class Rule implements Plugin<Project> {
		@Inject
		public Rule() {}

		@Override
		public void apply(Project project) {
			project.getPluginManager().apply(JBakeBasePlugin.class);
			project.getExtensions().configure(JBakeExtension.class, jbake -> {
				JBakeContentExtension content = jbake.getExtensions().create("content", JBakeContentExtension.class, Names.forMain());
				content.content.configure(it -> it.extendsFrom(jbake.getDependencies().getJBake().get()));
				jbake.getStageTask().configure(task -> task.into("content", spec -> spec.from(content.getAsFileTree())));
				jbake.getDependencies().getJBakeElements().configure(it -> {
					it.outgoing(outgoing -> {
						outgoing.getVariants().create("jbake-content-directory", variant -> {
							variant.artifact(content.getLocation(), t -> t.setType("jbake-content-directory"));
						});
					});
				});
			});

			project.getDependencies().registerTransform(UnpackJBakeContentTransform.class, spec -> {
				spec.getFrom().attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "jbake"));
				spec.getFrom().attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "jbake-directory");

				spec.getTo().attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "jbake"));
				spec.getTo().attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "jbake-content-directory");
			});
		}

		/*private*/ static abstract /*final*/ class UnpackJBakeContentTransform implements TransformAction<TransformParameters.None> {
			@Inject
			public UnpackJBakeContentTransform() {}

			@InputArtifact
			public abstract Provider<FileSystemLocation> getInputArtifact();

			@Override
			public void transform(TransformOutputs outputs) {
				File artifact = getInputArtifact().get().getAsFile();
				File contentDir = new File(artifact, "content");
				if (contentDir.exists()) {
					outputs.dir(contentDir);
				}
			}
		}
	}
}
