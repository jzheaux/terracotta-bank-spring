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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.joshcummings.codeplay.terracotta.model.User;
import com.joshcummings.codeplay.terracotta.service.UserService;

/**
 * This servlet makes Terracotta vulnerable to Cross-site Scripting because
 * it fails to validate the {@code forgotPasswordUsername} parameter and
 * needlessly reflects it back to the browser.
 *
 * It also leaks password information to the screen. And, further, it gives
 * sensitive information without validating ownership.
 *
 * It is vulnerable to CSRF because it naively overrides {@link this#doGet} to invoke
 * {@link this#doPost}
 *
 * It makes the site vulnerable to Enumeration since it responds differently
 * in the case that the user exists vs when the user does not exist.
 *
 * @author Josh Cummings
 *
 */
public class ForgotPasswordServlet extends HttpServlet {
	private final UserService userService;

	public ForgotPasswordServlet(UserService userService) {
		this.userService = userService;
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("forgotPasswordUsername");

		User user = this.userService.findByUsername(username);

		if ( user == null ) {
			request.setAttribute("forgotPasswordErrorMessage", "The user entered (" + username + ") does not exist.");
		} else {
			request.setAttribute("forgotPasswordErrorMessage", "The password is (" + user.getPassword() + ")");
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		super.doGet(request, response);
	}
}
