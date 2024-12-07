package net.nokeedev.jbake;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileTree;

public interface JBakeTemplates {
	FileTree getAsFileTree();
	DirectoryProperty getLocation();
}
