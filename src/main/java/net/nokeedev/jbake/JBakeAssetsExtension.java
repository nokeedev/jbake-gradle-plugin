package net.nokeedev.jbake;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileTree;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;
import java.util.concurrent.Callable;

/*
jbake {
  assets { from('templates') }
  assets { from('templates') { into('libs') } }
  assets { from('foo') { rename ... } }
  assets project(':foo')
}
 */
public abstract /*final*/ class JBakeAssetsExtension implements JBakeAssets {
	private final TaskProvider<Sync> assembleTask;
	private final ConfigurableFileCollection files;
	private final NamedDomainObjectProvider<Configuration> assets;

	@Inject
	public JBakeAssetsExtension(Names names, ObjectFactory objects, ConfigurationContainer configurations, TaskContainer tasks) {
		this.assembleTask = tasks.register(names.taskName("assemble", "JBaseAssets"), Sync.class);
		this.assets = configurations.register(names.configurationName("assets"));

		assets.configure(new AsDeclarable(it -> {}));
		assets.configure(new ConfigureJBakeExtensionDescription("Assets", it -> {}));

		this.assembleTask.configure(task -> {
			task.setDestinationDir(task.getTemporaryDir());
		});

		NamedDomainObjectProvider<Configuration> incomingAssets = configurations.register(names.configurationName("jbakeAssets"));
		incomingAssets.configure(new AsResolvable(it -> {}));
		incomingAssets.configure(it -> it.extendsFrom(assets.get()));
		incomingAssets.configure(new JBakeAssetsConfiguration(objects));
		incomingAssets.configure(new ConfigureJBakeExtensionDescription("Assets", it -> {}));
		incomingAssets.configure(new ResolveAsDirectoryArtifact("jbake-assets-directory"));

		this.files = objects.fileCollection().from(assembleTask).from((Callable<?>) () -> {
			return incomingAssets.get().getIncoming().artifactView(it -> {
				it.attributes(attributes -> {
					attributes.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "jbake-assets-directory");
				});
			}).getFiles();
		});

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
		return assets.get();
	}

	/*private*/ static abstract /*final*/ class Rule implements Plugin<Project> {
		@Inject
		public Rule() {}

		@Override
		public void apply(Project project) {
			project.getPluginManager().apply(JBakeBasePlugin.class);
			project.getExtensions().configure(JBakeExtension.class, jbake -> {
				JBakeAssetsExtension assets = jbake.getExtensions().create("assets", JBakeAssetsExtension.class, Names.forMain());
				assets.assets.configure(it -> it.extendsFrom(jbake.getDependencies().getJBake().get()));
				jbake.getStageTask().configure(task -> task.into("assets", spec -> spec.from(assets.getAsFileTree())));
				jbake.getDependencies().getJBakeElements().configure(it -> {
					it.outgoing(outgoing -> {
						outgoing.getVariants().create("jbake-assets-directory", variant -> {
							variant.artifact(assets.getLocation(), t -> t.setType("jbake-assets-directory"));
						});
					});
				});
			});
		}
	}
}
