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
package com.joshcummings.codeplay.terracotta.servlet;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.joshcummings.codeplay.terracotta.model.User;
import org.h2.jdbcx.JdbcDataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Exposing as a servlet for demo purposes since Terracotta uses an in-memory
 * database by default.
 */
public class PasswordUpgradeServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setURL("jdbc:h2:mem:db");
		dataSource.setUser("user");
		dataSource.setPassword("password");

		JdbcTemplate jdbc = new JdbcTemplate(dataSource);

		List<User> users =
				jdbc.query(
						"SELECT id, username, password, name, email FROM users",
						(rs, rowNum) ->
								new User(
										rs.getString(1),
										rs.getString(2),
										rs.getString(3),
										rs.getString(4),
										rs.getString(5)));

		for ( User user : users ) {
			String plaintext = user.getPassword();
			String hashed = encoder.encode(plaintext);
			jdbc.update("UPDATE users SET password = ? WHERE id = ?",
					hashed, user.getId());
		}

		resp.getWriter().println("OK");
	}
}
