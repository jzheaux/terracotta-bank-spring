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
import java.math.BigDecimal;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.joshcummings.codeplay.terracotta.model.Account;
import com.joshcummings.codeplay.terracotta.model.User;
import com.joshcummings.codeplay.terracotta.service.AccountService;
import com.joshcummings.codeplay.terracotta.service.EmailService;
import com.joshcummings.codeplay.terracotta.service.UserService;
import com.joshcummings.codeplay.terracotta.service.passwords.Evaluation;
import com.joshcummings.codeplay.terracotta.service.passwords.PasswordEntropyEvaluator;
import com.joshcummings.codeplay.terracotta.service.passwords.WeakPasswordEntropyEvaluator;

import static com.j256.twofactorauth.TimeBasedOneTimePasswordUtil.generateBase32Secret;
import static com.j256.twofactorauth.TimeBasedOneTimePasswordUtil.qrImageUrl;

/**
 * This class makes Terracotta Bank vulnerable to Enumeration
 * attacks because it reveals when a username is an existing
 * username in the system.
 *
 * It also makes the site vulnerable to Persisted Cross-site
 * Scripting because it doesn't validate and encode the user's
 * registration information before persisting it.
 *
 * @author Josh Cummings
 */
//@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Long nextUserNumber = 5L;
	private Long nextAccountNumber = 5L;

	private AccountService accountService;
	private UserService userService;
	private EmailService emailService;

	private PasswordEntropyEvaluator entropyEvaluator = new WeakPasswordEntropyEvaluator();

	public RegisterServlet(AccountService accountService, UserService userService, EmailService emailService) {
		this.accountService = accountService;
		this.userService = userService;
		this.emailService = emailService;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getRequestDispatcher("/webapp/WEB-INF/register.jsp").forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("registerUsername");
		String password = request.getParameter("registerPassword");
		String name = request.getParameter("registerName");
		String email = request.getParameter("registerEmail");

		Evaluation evaluation = this.entropyEvaluator.evaluate(password);
		if ( !evaluation.isSuccess() ) {
			request.setAttribute("registrationErrorMessage",
					"Your password (" + password + ") isn't strong enough: <br/>" +
							evaluation.getDetails().stream().collect(Collectors.joining("<br/>")));
			request.getRequestDispatcher(request.getContextPath() + "index.jsp").forward(request, response);
			return;
		}

		boolean secondFactor = request.getParameter("useSecondFactor") != null;

		String secret = null;
		if ( secondFactor ) {
			secret = generateBase32Secret();
		}

		User user = new User(String.valueOf(this.nextUserNumber++), username, password, name, email, secret);
		Account account = new Account(String.valueOf(this.nextAccountNumber++),
								new BigDecimal("25"),
								this.nextAccountNumber++,
								user.getId());

		try {
			this.userService.addUser(user);
			this.accountService.addAccount(account);

			String message = "Your account number is: <b>" + account.getNumber() + "</b><br/>" +
					"Your password is: <b>" + user.getPassword() + "</b><br/>";

			if ( secondFactor ) {
				message += "You have signed up for two-factor authentication, good job! " +
						"Scan this QR code using Google Authenticator to link the app with our site.<br/>" +
						"<img src=\"" + qrImageUrl("Terracotta%20Bank", secret) + "\"/><br/>";
			}

			message += "Sincerely, Terracotta Bank Management";

			this.emailService.sendMessage(email, "Welcome to Terracotta Bank!", message);

			request.getSession().setAttribute("authenticatedUser", user);
			request.getSession().setAttribute("authenticatedAccount", account);
			response.sendRedirect(request.getContextPath() + "/index.jsp");
		}
		catch ( IllegalArgumentException e )
		{
			request.setAttribute("registrationErrorMessage", "That username is already taken");
			request.getRequestDispatcher(request.getContextPath() + "index.jsp").forward(request, response);
		}
	}

}
