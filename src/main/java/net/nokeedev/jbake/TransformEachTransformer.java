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

import org.gradle.api.Transformer;

import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

final class TransformEachTransformer<OUT, IN> implements Transformer<Iterable<OUT>, Iterable<IN>> {
	private final Transformer<? extends OUT, ? super IN> mapper;

	public TransformEachTransformer(Transformer<? extends OUT, ? super IN> mapper) {
		this.mapper = mapper;
	}

	@Override
	public Iterable<OUT> transform(Iterable<IN> it) {
		return StreamSupport.stream(it.spliterator(), false).map(mapper::transform).collect(toList());
	}
}
