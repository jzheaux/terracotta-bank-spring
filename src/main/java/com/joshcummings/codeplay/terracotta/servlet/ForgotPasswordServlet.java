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
import java.util.concurrent.ExecutorService;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.joshcummings.codeplay.terracotta.model.Transaction;
import com.joshcummings.codeplay.terracotta.model.User;
import com.joshcummings.codeplay.terracotta.service.EmailService;
import com.joshcummings.codeplay.terracotta.service.TransactionService;
import com.joshcummings.codeplay.terracotta.service.UserService;

/**
 * This servlet makes Terracotta vulnerable to Cross-site Scripting because
 * it fails to validate the {@code forgotPasswordAccount} parameter and
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
	private final EmailService emailService;
	private final TransactionService transactionService;

	public ForgotPasswordServlet(
			UserService userService,
			EmailService emailService,
			TransactionService transactionService) {

		this.userService = userService;
		this.emailService = emailService;
		this.transactionService = transactionService;
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("forgotPasswordAccount");

		User user = this.userService.findByUsername(username);

		if ( user == null ) {
			send(request, response, "We've sent password reset instructions to your account.", 200);
		} else {
			Transaction changePassword = this.transactionService.beginTransaction(user, "change_password");
			this.emailService.sendMessage(user.getEmail(), "Password Change Instructions",
					"You are receiving this because there was a password change request made to your account.\n\n" +
					"If you didn't request this change, please contact Terracotta Bank immediately.\n\n"+
					"Otherwise, please click this link to be directed to the password change form:\n\n" +
					"<a href='http://localhost:8080/changePassword?key=" + changePassword.getKey() + "'>" +
							"Change Your Password" +
					"</a>.");
			send(request, response, "We've sent password reset instructions to your account.", 200);
		}
	}

	private void send(HttpServletRequest request, HttpServletResponse response, String error, int status)
			throws ServletException, IOException {

		response.setStatus(status);
		request.setAttribute("message", error);
		request.getRequestDispatcher("/WEB-INF/json/error.jsp").forward(request, response);
	}
}
