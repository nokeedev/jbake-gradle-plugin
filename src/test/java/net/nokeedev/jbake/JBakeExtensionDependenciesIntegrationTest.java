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

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.DocsType;
import org.gradle.api.attributes.Usage;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static net.nokeedev.jbake.NamedMatcher.named;
import static net.nokeedev.jbake.ProviderMatcher.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JBakeExtensionDependenciesIntegrationTest {
	private final Project project = createProject();
	private final JBakeDependencies subject = JBakeExtensionFactory.forProject(project).create("lkes").getDependencies();

	private static Project createProject() {
		final Project project = ProjectBuilder.builder().build();
		project.setGroup("com.example");
		project.setVersion("1.5");
		return project;
	}

	interface AssetsAttributesTester {
		Configuration subject();

		@Test
		default void hasUsageAttribute() {
			assertEquals("jbake-assets", subject().getAttributes().getAttribute(Usage.USAGE_ATTRIBUTE).getName());
		}

		@Test
		default void hasDocTypeAttribute() {
			assertEquals("jbake-assets", subject().getAttributes().getAttribute(DocsType.DOCS_TYPE_ATTRIBUTE).getName());
		}
	}

	interface TemplatesAttributesTester {
		Configuration subject();

		@Test
		default void hasUsageAttribute() {
			assertEquals("jbake-templates", subject().getAttributes().getAttribute(Usage.USAGE_ATTRIBUTE).getName());
		}

		@Test
		default void hasDocTypeAttribute() {
			assertEquals("jbake-templates", subject().getAttributes().getAttribute(DocsType.DOCS_TYPE_ATTRIBUTE).getName());
		}
	}

	interface ContentAttributesTester {
		Configuration subject();

		@Test
		default void hasUsageAttribute() {
			assertEquals("jbake-content", subject().getAttributes().getAttribute(Usage.USAGE_ATTRIBUTE).getName());
		}

		@Test
		default void hasDocTypeAttribute() {
			assertEquals("jbake-content", subject().getAttributes().getAttribute(DocsType.DOCS_TYPE_ATTRIBUTE).getName());
		}
	}

	interface ConfigurationAttributesTester {
		Configuration subject();

		@Test
		default void hasUsageAttribute() {
			assertEquals("jbake-properties", subject().getAttributes().getAttribute(Usage.USAGE_ATTRIBUTE).getName());
		}

		@Test
		default void hasDocTypeAttribute() {
			assertEquals("jbake-properties", subject().getAttributes().getAttribute(DocsType.DOCS_TYPE_ATTRIBUTE).getName());
		}
	}

	interface DeclarableConfigurationTester {
		Configuration subject();

		@Test
		default void isDeclarable() {
			assertFalse(subject().isCanBeConsumed());
			assertFalse(subject().isCanBeResolved());
		}
	}

	@Nested
	class JBakeConfigurationTest implements DeclarableConfigurationTester {
		public Configuration subject() {
			return project.getConfigurations().getByName("lkesJbake");
		}

		@Test
		void hasDescription() {
			assertEquals("JBake dependencies for JBake extension.", subject().getDescription());
		}

		@Test
		void canAccessConfigurationViaExtensionDependencies() {
			assertThat(subject.getJBake(), providerOf(sameInstance(subject())));
		}
	}

	interface ResolvableConfigurationTester {
		Configuration subject();

		@Test
		default void isResolvable() {
			assertFalse(subject().isCanBeConsumed());
			assertTrue(subject().isCanBeResolved());
		}

		@Test
		default void extendsFromJBakeDeclarableBucket() {
			assertThat(subject().getExtendsFrom(), contains(named("lkesJbake")));
		}
	}

	@Nested
	class AssetsConfigurationTest implements ResolvableConfigurationTester, AssetsAttributesTester {
		public Configuration subject() {
			return project.getConfigurations().getByName("lkesAssets");
		}

		@Test
		void hasDescription() {
			assertEquals("Assets for JBake extension.", subject().getDescription());
		}

		@Test
		void canAccessConfigurationViaExtensionDependencies() {
			assertThat(subject.getAssets(), providerOf(sameInstance(subject())));
		}
	}

	@Nested
	class TemplatesConfigurationTest implements ResolvableConfigurationTester, TemplatesAttributesTester {
		public Configuration subject() {
			return project.getConfigurations().getByName("lkesTemplates");
		}

		@Test
		void hasDescription() {
			assertEquals("Templates for JBake extension.", subject().getDescription());
		}

		@Test
		void canAccessConfigurationViaExtensionDependencies() {
			assertThat(subject.getTemplates(), providerOf(sameInstance(subject())));
		}
	}

	@Nested
	class ContentConfigurationTest implements ResolvableConfigurationTester, ContentAttributesTester {
		public Configuration subject() {
			return project.getConfigurations().getByName("lkesContent");
		}

		@Test
		void hasDescription() {
			assertEquals("Content for JBake extension.", subject().getDescription());
		}

		@Test
		void canAccessConfigurationViaExtensionDependencies() {
			assertThat(subject.getContent(), providerOf(sameInstance(subject())));
		}
	}

	@Nested
	class ConfigurationConfigurationTest implements ResolvableConfigurationTester, ConfigurationAttributesTester {
		public Configuration subject() {
			return project.getConfigurations().getByName("lkesConfiguration");
		}

		@Test
		void hasDescription() {
			assertEquals("Configuration for JBake extension.", subject().getDescription());
		}

		@Test
		void canAccessConfigurationViaExtensionDependencies() {
			assertThat(subject.getProperties(), providerOf(sameInstance(subject())));
		}
	}

	interface ConsumableConfigurationTester {
		Configuration subject();

		@Test
		default void isConsumable() {
			assertTrue(subject().isCanBeConsumed());
			assertFalse(subject().isCanBeResolved());
		}

		@Test
		default void hasCapability() {
			System.out.println(subject().getOutgoing().getCapabilities());
//			assertThat(subject().getOutgoing().getCapabilities(), hasItem(coordinate("com.example:lkes:1.5")));
		}
	}

	@Nested
	class AssetsElementsConfigurationTest implements ConsumableConfigurationTester, AssetsAttributesTester {
		public Configuration subject() {
			return project.getConfigurations().getByName("lkesAssetsElements");
		}

		@Test
		void hasDescription() {
			assertEquals("Assets elements for JBake extension.", subject().getDescription());
		}

		@Test
		void canAccessConfigurationViaExtensionDependencies() {
			assertThat(subject.getAssetsElements(), providerOf(sameInstance(subject())));
		}
	}

	@Nested
	class TemplatesElementsConfigurationTest implements ConsumableConfigurationTester, TemplatesAttributesTester {
		public Configuration subject() {
			return project.getConfigurations().getByName("lkesTemplatesElements");
		}

		@Test
		void hasDescription() {
			assertEquals("Templates elements for JBake extension.", subject().getDescription());
		}

		@Test
		void canAccessConfigurationViaExtensionDependencies() {
			assertThat(subject.getTemplatesElements(), providerOf(sameInstance(subject())));
		}
	}

	@Nested
	class ContentElementsConfigurationTest implements ConsumableConfigurationTester, ContentAttributesTester {
		public Configuration subject() {
			return project.getConfigurations().getByName("lkesContentElements");
		}

		@Test
		void hasDescription() {
			assertEquals("Content elements for JBake extension.", subject().getDescription());
		}

		@Test
		void canAccessConfigurationViaExtensionDependencies() {
			assertThat(subject.getContentElements(), providerOf(sameInstance(subject())));
		}
	}

	@Nested
	class ConfigurationElementsConfigurationTest implements ConsumableConfigurationTester, ConfigurationAttributesTester {
		public Configuration subject() {
			return project.getConfigurations().getByName("lkesConfigurationElements");
		}

		@Test
		void hasDescription() {
			assertEquals("Configuration elements for JBake extension.", subject().getDescription());
		}

		@Test
		void canAccessConfigurationViaExtensionDependencies() {
			assertThat(subject.getPropertiesElements(), providerOf(sameInstance(subject())));
		}
	}

	@Nested
	class BakedElementsConfigurationTest implements ConsumableConfigurationTester {
		public Configuration subject() {
			return project.getConfigurations().getByName("lkesBakedElements");
		}

		@Test
		void hasDescription() {
			assertEquals("Baked elements for JBake extension.", subject().getDescription());
		}

		@Test
		void hasUsageAttribute() {
			assertEquals("jbake-baked", subject().getAttributes().getAttribute(Usage.USAGE_ATTRIBUTE).getName());
		}

		@Test
		void hasDocTypeAttribute() {
			assertEquals("jbake-baked", subject().getAttributes().getAttribute(DocsType.DOCS_TYPE_ATTRIBUTE).getName());
		}

		@Test
		void canAccessConfigurationViaExtensionDependencies() {
			assertThat(subject.getBakedElements(), providerOf(sameInstance(subject())));
		}
	}
}
