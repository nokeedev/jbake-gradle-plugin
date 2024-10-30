package org.gradle.api.tasks;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Attached to an input property to specify that directories should be ignored
 * when snapshotting inputs. Files within directories and subdirectories will be
 * snapshot, but the directories themselves will be ignored. Empty directories,
 * and directories that contain only empty directories will have no effect on the
 * resulting snapshot.
 *
 * <p>This annotation should be attached to the getter method in Java or the property in Groovy.
 * Annotations on setters or just the field in Java are ignored.</p>
 *
 * <p>This annotation can be used with the following annotations:</p>
 *
 * <ul><li>{@link org.gradle.api.tasks.InputFiles}</li>
 *
 * <li>{@link org.gradle.api.tasks.InputDirectory}</li>
 *
 * <li>{@link org.gradle.api.tasks.SkipWhenEmpty}</li>
 *
 * <li>{@link org.gradle.api.artifacts.transform.InputArtifact}</li>
 *
 * <li>{@link org.gradle.api.artifacts.transform.InputArtifactDependencies}</li> </ul>
 *
 * @since 6.8
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface IgnoreEmptyDirectories {
}