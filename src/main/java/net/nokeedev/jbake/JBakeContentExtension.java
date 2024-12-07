package net.nokeedev.jbake;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
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
		incomingContent.configure(new ResolveAsDirectoryArtifact("jbake-content-directory"));

		this.files = objects.fileCollection().from(assembleTask).from((Callable<?>) incomingContent::get);

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
				jbake.getDependencies().getContentElements().configure(it -> it.extendsFrom(content.asConfiguration()));
				jbake.getDependencies().getContentElements().configure(new FileCollectionArtifact(project, content.getLocation().getAsFileTree(), "jbake-content"));
			});
		}
	}
}
