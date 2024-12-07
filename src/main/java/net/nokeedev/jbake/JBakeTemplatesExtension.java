package net.nokeedev.jbake;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
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
		incomingTemplates.configure(new ResolveAsDirectoryArtifact("jbake-templates-directory"));

		this.files = objects.fileCollection().from(assembleTask).from((Callable<?>) incomingTemplates::get);

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
				jbake.getDependencies().getTemplatesElements().configure(it -> it.extendsFrom(templates.asConfiguration()));
				jbake.getDependencies().getTemplatesElements().configure(new FileCollectionArtifact(project, templates.getLocation().getAsFileTree(), "jbake-templates"));
			});
		}
	}
}
