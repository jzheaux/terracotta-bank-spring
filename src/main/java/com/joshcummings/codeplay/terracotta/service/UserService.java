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

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Set;

/**
 * This class makes Terracotta Bank vulnerable to SQL injection
 * attacks because it concatenates queries instead of using
 * bind variables.
 *
 * @author Josh Cummings
 */
@Service
public class UserService extends ServiceSupport {
	public void addUser(User user) {
		runUpdate("INSERT INTO users (id, username, password, name, email, totp_secret)"
				+ " VALUES (?, ?, ?, ?, ?, ?)",
				ps -> {
					ps.setString(1, user.getId());
					ps.setString(2, user.getUsername());
					ps.setString(3, user.getPassword());
					ps.setString(4, user.getName());
					ps.setString(5, user.getEmail());
					ps.setString(6, user.getTotpSecret());
					return ps;
				});
	}

	public User findByUsername(String username) {
		Set<User> users = runQuery("SELECT * FROM users WHERE username = '" + username + "'",
			(rs) -> {
				try {
					return new User(rs.getString(1), rs.getString(4), rs.getString(5), rs.getString(2), rs.getString(3), rs.getString(7));
				} catch ( SQLException e ) {
					throw new IllegalStateException(e);
				}
			});
		return users.isEmpty() ? null : users.iterator().next();
	}

	public User findByUsernameAndPassword(String username, String password) {
		Set<User> users = runQuery("SELECT * FROM users WHERE username = '" + username + "' AND password = '" + password + "'",
				(rs) -> {
					try {
						return new User(rs.getString(1), rs.getString(4), rs.getString(5), rs.getString(2), rs.getString(3), rs.getString(7));
					} catch ( SQLException e ) {
						throw new IllegalStateException(e);
					}
				});
		return users.isEmpty() ? null : users.iterator().next();
	}

	public Integer count() {
		return super.count("users");
	}

	@PreAuthorize("hasRole('ADMIN')")
	public void updateUser(User user) {
		runUpdate("UPDATE users SET name = '" + user.getName() + "', email = '" + user.getEmail() + "' "+
					"WHERE id = '" + user.getId() + "'");
	}

	@PreAuthorize("hasRole('ADMIN')")
	public void updateUserPassword(User user) {
		runUpdate("UPDATE users SET password = '" + user.getPassword() + "' WHERE id = '" + user.getId() + "'");
	}

	@PreAuthorize("hasRole('ADMIN')")
	public void removeUser(String username) {
		runUpdate("DELETE FROM users WHERE username = '" + username + "'");
	}

	public int maybeChangeAdminPassword(String s) {
		return runUpdate(
				"UPDATE users SET password = ? WHERE username = 'admin' AND password = 'admin'",
				ps -> {
					ps.setString(1, s);
					return ps;
				});
	}
}
