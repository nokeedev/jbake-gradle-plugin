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

public abstract class JBakeTemplatesExtension implements JBakeTemplates {
	private final TaskProvider<Sync> assembleTask;
	private final ConfigurableFileCollection files;
	private final NamedDomainObjectProvider<Configuration> templates;

	@Inject
	public JBakeTemplatesExtension(Names names, ObjectFactory objects, ConfigurationContainer configurations, TaskContainer tasks) {
		this.assembleTask = tasks.register(names.taskName("assemble", "JBaseTemplates"), Sync.class);
		this.templates = configurations.register(names.configurationName("templates"));

		templates.configure(new AsDeclarable(it -> {}));
		templates.configure(new ConfigureJBakeExtensionDescription("Templates", it -> {}));

		this.assembleTask.configure(task -> {
			task.setDestinationDir(task.getTemporaryDir());
		});

		NamedDomainObjectProvider<Configuration> incomingTemplates = configurations.register(names.configurationName("jbakeTemplates"));
		incomingTemplates.configure(new AsResolvable(it -> {}));
		incomingTemplates.configure(it -> it.extendsFrom(templates.get()));
		incomingTemplates.configure(new JBakeTemplatesConfiguration(objects));
		incomingTemplates.configure(new ConfigureJBakeExtensionDescription("Templates", it -> {}));

		this.files = objects.fileCollection().from(assembleTask)
			.from(incomingTemplates.get().getIncoming().artifactView(new ResolveAsDirectoryArtifact("jbake-templates-directory")).getFiles());

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
		return templates.get();
	}

	/*private*/ static abstract /*final*/ class Rule implements Plugin<Project> {
		@Inject
		public Rule() {}

		@Override
		public void apply(Project project) {
			project.getPluginManager().apply(JBakeBasePlugin.class);
			project.getExtensions().configure(JBakeExtension.class, jbake -> {
				JBakeTemplatesExtension templates = jbake.getExtensions().create("templates", JBakeTemplatesExtension.class, Names.forMain());
				templates.templates.configure(it -> it.extendsFrom(jbake.getDependencies().getJBake().get()));
				jbake.getStageTask().configure(task -> task.into("templates", spec -> spec.from(templates.getAsFileTree())));
				jbake.getDependencies().getJBakeElements().configure(it -> {
					it.outgoing(outgoing -> {
						outgoing.getVariants().create("jbake-templates-directory", variant -> {
							variant.artifact(templates.getLocation(), t -> t.setType("jbake-templates-directory"));
						});
					});
				});
			});

			project.getDependencies().registerTransform(UnpackJBakeTemplatesTransform.class, spec -> {
				spec.getFrom().attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "jbake"));
				spec.getFrom().attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "jbake-directory");

				spec.getTo().attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "jbake"));
				spec.getTo().attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "jbake-templates-directory");
			});
		}

		/*private*/ static abstract /*final*/ class UnpackJBakeTemplatesTransform implements TransformAction<TransformParameters.None> {
			@Inject
			public UnpackJBakeTemplatesTransform() {}

			@InputArtifact
			public abstract Provider<FileSystemLocation> getInputArtifact();

			@Override
			public void transform(TransformOutputs outputs) {
				File artifact = getInputArtifact().get().getAsFile();
				File contentDir = new File(artifact, "templates");
				if (contentDir.exists()) {
					outputs.dir(contentDir);
				}
			}
		}
	}
}
