/*
 * Copyright 2015-2018 Josh Cummings
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.joshcummings.codeplay.terracotta.service.passwords;

import org.springframework.security.crypto.password.PasswordEncoder;

public class WrappingPasswordEncoder implements PasswordEncoder {
	private final PasswordEncoder existing;
	private final PasswordEncoder wrapper;

	public WrappingPasswordEncoder(PasswordEncoder existing, PasswordEncoder wrapper) {
		this.existing = existing;
		this.wrapper = wrapper;
	}

	@Override
	public String encode(CharSequence rawPassword) {
		return this.wrapper.encode(
				this.existing.encode(rawPassword));
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		return this.wrapper.matches(
				this.existing.encode(rawPassword), encodedPassword);
	}
}
