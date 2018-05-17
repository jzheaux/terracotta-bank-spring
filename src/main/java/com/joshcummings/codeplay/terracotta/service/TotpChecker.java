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
package com.joshcummings.codeplay.terracotta.service;

import com.joshcummings.codeplay.terracotta.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;

import static com.j256.twofactorauth.TimeBasedOneTimePasswordUtil.generateBase32Secret;
import static com.j256.twofactorauth.TimeBasedOneTimePasswordUtil.validateCurrentNumber;

/**
 * For verifying the given TOTP on login
 *
 * Note that we also perform the calculation even if we couldn't find the user
 * as Enumeration defense.
 * 
 * @author Josh Cummings
 */
public class TotpChecker {
	private static final String NO_ONE = generateBase32Secret();

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public boolean check(User user, int code) {
		String secret = NO_ONE;
		if ( user != null && user.getTotpSecret() != null ) {
			secret = user.getTotpSecret();
		}

		try {
			boolean passes = validateCurrentNumber(secret, code, 30);
			return user != null && ( passes || user.getTotpSecret() == null );
		} catch ( GeneralSecurityException e ) {
			this.log.warn("Unable to validate totp code [{}] " +
					"for user with id [{}]", code, user.getId());
			return false;
		}
	}
}
