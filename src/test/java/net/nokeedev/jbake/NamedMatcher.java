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

import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.equalTo;

public final class NamedMatcher extends FeatureMatcher<Object, String> {
	public NamedMatcher(Matcher<? super String> subMatcher) {
		super(subMatcher, "has named", "named");
	}

	@Override
	protected String featureValueOf(Object actual) {
		if (actual instanceof Configuration) {
			return ((Configuration) actual).getName();
		} else if (actual instanceof Task) {
			return ((Task) actual).getName();
		}
		throw new UnsupportedOperationException(String.format("Object '%s' of type %s is not named-able.", actual, actual.getClass().getCanonicalName()));
	}

	public static Matcher<Object> named(String name) {
		return new NamedMatcher(equalTo(name));
	}
}
