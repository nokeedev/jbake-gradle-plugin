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

import org.gradle.api.capabilities.Capability;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.equalTo;

public final class CoordinateMatcher extends FeatureMatcher<Capability, String> {
	public CoordinateMatcher(Matcher<? super String> subMatcher) {
		super(subMatcher, "", "");
	}

	@Override
	protected String featureValueOf(Capability actual) {
		if (actual.getVersion() == null) {
			return actual.getGroup() + ":" + actual.getName();
		} else {
			return actual.getGroup() + ":" + actual.getName() + ":" + actual.getVersion();
		}
	}

	public static Matcher<Capability> coordinate(String c) {
		return new CoordinateMatcher(equalTo(c));
	}
}
