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
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;
import java.io.File;
import java.util.Map;
import java.util.Set;

public class JBakePropertiesExtension implements JBakeProperties {
	private final TaskProvider<GenerateJBakeProperties> assembleTask;
	private final NamedDomainObjectProvider<Configuration> properties;
	private final MapProperty<String, Object> elements;
	private final MapProperty<String, Object> allElements;

	@Inject
	public JBakePropertiesExtension(Names names, ObjectFactory objects, ConfigurationContainer configurations, TaskContainer tasks) {
		this.properties = configurations.register(names.configurationName("configuration"));

		properties.configure(new AsDeclarable(it -> {}));
		properties.configure(new ConfigureJBakeExtensionDescription("Configuration", it -> {}));

		this.elements = objects.mapProperty(String.class, Object.class);
		this.allElements = objects.mapProperty(String.class, Object.class);

		NamedDomainObjectProvider<Configuration> incomingAssets = configurations.register(names.configurationName("jbakeProperties"));
		incomingAssets.configure(new AsResolvable(it -> {}));
		incomingAssets.configure(it -> it.extendsFrom(properties.get()));
		incomingAssets.configure(new JBakePropertiesConfiguration(objects));
		incomingAssets.configure(new ConfigureJBakeExtensionDescription("Configuration", it -> {}));

		Provider<Set<FileSystemLocation>> propertiesFiles = incomingAssets.get().getIncoming().artifactView(new ResolveAsDirectoryArtifact("jbake-properties")).getFiles().getElements();
		allElements.putAll(propertiesFiles.map(new TransformEachTransformer<>(new LoadPropertiesFileIfAvailable())).map(new MergeJBakePropertiesTransformer()));
		allElements.putAll(elements);

		this.assembleTask = tasks.register(names.taskName("assemble", "JBakeProperties"), GenerateJBakeProperties.class);
		this.assembleTask.configure(task -> {
			// The statement passed to allElements.putAll is correct but the dependencies are lost within allElements
			task.dependsOn(propertiesFiles);

			task.getConfigurations().putAll(allElements);
			task.getOutputFile().fileValue(new File(task.getTemporaryDir(), "jbake.properties"));
		});
//		this.files = objects.fileCollection().from(assembleTask).from((Callable<?>) incomingAssets::get);

//		getLocation().fileProvider(assembleTask.map(Sync::getDestinationDir));
//		getLocation().disallowChanges();
	}

	@Override
	public Provider<Map<String, Object>> getElements() {
		return elements;
	}

	@Override
	public Provider<Map<String, Object>> getAllElements() {
		return allElements;
	}

	public void configure(Action<? super MapProperty<String, Object>> action) {
		action.execute(elements);
	}

	private Configuration asConfiguration() {
		return properties.get();
	}

	/*private*/ static abstract /*final*/ class Rule implements Plugin<Project> {
		private final TaskContainer tasks;
		private final ProjectLayout layout;

		@Inject
		public Rule(TaskContainer tasks, ProjectLayout layout) {
			this.tasks = tasks;
			this.layout = layout;
		}

		@Override
		public void apply(Project project) {
			project.getPluginManager().apply(JBakeBasePlugin.class);
			project.getExtensions().configure(JBakeExtension.class, jbake -> {
				JBakePropertiesExtension properties = jbake.getExtensions().create("configurations", JBakePropertiesExtension.class, Names.forMain());
				properties.properties.configure(it -> it.extendsFrom(jbake.getDependencies().getJBake().get()));
				jbake.getStageTask().configure(task -> task.from(properties.assembleTask));
				jbake.getBakeTask().configure(task -> {
					task.getConfigurations().putAll(properties.getAllElements());
				});
				jbake.getDependencies().getJBakeElements().configure(it -> {
					it.outgoing(outgoing -> {
						outgoing.getVariants().create("jbake-properties", variant -> {
							// Because publish artifacts gets queried early, we can't flat map the output property.
							//   Instead, we map the task to the property value... it's a workaround to achieve the same thing:
							//   aka. a provider to the output file with implicit task dependency
							variant.artifact(properties.assembleTask, t -> t.setType("jbake-properties"));
						});
					});
				});
			});

			project.getDependencies().registerTransform(UnpackJBakePropertiesTransform.class, spec -> {
				spec.getFrom().attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "jbake"));
				spec.getFrom().attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "jbake-directory");

				spec.getTo().attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "jbake"));
				spec.getTo().attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "jbake-properties");
			});
		}

		/*private*/ static abstract /*final*/ class UnpackJBakePropertiesTransform implements TransformAction<TransformParameters.None> {
			@Inject
			public UnpackJBakePropertiesTransform() {}

			@InputArtifact
			public abstract Provider<FileSystemLocation> getInputArtifact();

			@Override
			public void transform(TransformOutputs outputs) {
				File artifact = getInputArtifact().get().getAsFile();
				File file = new File(artifact, "jbake.properties");
				if (file.exists()) {
					outputs.file(file);
				}
			}
		}
	}
}
