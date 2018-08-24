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
package com.joshcummings.codeplay.terracotta;

import com.joshcummings.codeplay.terracotta.model.Transaction;
import com.joshcummings.codeplay.terracotta.model.User;
import com.joshcummings.codeplay.terracotta.service.TransactionService;
import com.joshcummings.codeplay.terracotta.service.UserService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.apache.http.client.methods.RequestBuilder.get;
import static org.apache.http.client.methods.RequestBuilder.post;

public class ChangePasswordFunctionalTest extends AbstractEmbeddedTomcatTest {
	@BeforeMethod(alwaysRun = true)
	public void doLogin() {
		UserService userService = this.context.getBean(UserService.class);
		userService.addUser(new User("0", "user", "password", "User", "user@username"));
		login("user", "password");
	}

	@AfterMethod(alwaysRun = true)
	public void doLogout() {
		logout();
		UserService userService = this.context.getBean(UserService.class);
		userService.removeUser("user");
	}

	@Test
	public void testChangePasswordRequiresOldPassword() {
		int status = http.postForStatus(post("/changePassword")
			.addParameter("changePassword", "Longh0rn#p@cifiers!")
			.addParameter("verifyChangePassword", "Longh0rn#p@cifiers!"));

		Assert.assertEquals(status, 400);

		status = http.postForStatus(post("/changePassword")
			.addParameter("oldPassword", "password")
			.addParameter("changePassword", "Longh0rn#p@cifiers!")
			.addParameter("verifyChangePassword", "Longh0rn#p@cifiers!"));

		Assert.assertEquals(status, 200);
	}
	
	@Test
	public void testChangePasswordRequiresOldPasswordToBeCorrect() {
		int status = http.postForStatus(post("/changePassword")
				.addParameter("oldPassword", "wrongpassword")
				.addParameter("changePassword", "Longh0rn#p@cifiers!")
				.addParameter("verifyChangePassword", "Longh0rn#p@cifiers!"));

		Assert.assertEquals(status, 400);
	}

	@Test
	public void testChangePasswordCannotBePerformedWithGet() throws Exception {
		try ( CloseableHttpResponse response =
				http.getForEntity(get("/changePassword")
					.addParameter("oldPassword", "password")
					.addParameter("changePassword", "Longh0rn#p@cifiers!")
					.addParameter("verifyChangePassword", "Longh0rn#p@cifiers!")) ) {

			Assert.assertEquals(response.getStatusLine().getStatusCode(), 405);

			logout();
			String content = login("user", "password");

			Assert.assertTrue(content.contains("Welcome, User"));
			Assert.assertFalse(content.contains("password is incorrect"));
		}
	}

	@Test
	public void testChangePasswordAcceptsTransactionKeyAsOldPasswordSubstitute() throws Exception {
		TransactionService transactionService = this.context.getBean(TransactionService.class);
		UserService userService = this.context.getBean(UserService.class);
		User user = userService.findByUsername("user");

		Transaction transaction =
				transactionService.beginTransaction(user, "change_password");

		try ( CloseableHttpResponse response =
					  http.getForEntity(get("/changePassword")
							  .addParameter("key", transaction.getKey())
							  .addParameter("changePassword", "Longh0rn#p@cifiers!")
							  .addParameter("verifyChangePassword", "Longh0rn#p@cifiers!")) ) {

			logout();
			String content = login("user", "password");

			Assert.assertFalse(content.contains("Welcome, John"));
			Assert.assertTrue(content.contains("password is incorrect"));
		}

		Assert.assertNull(transactionService.retrieveTransaction(transaction.getKey()));
	}

	@Test
	public void testChangePasswordRejectsInvalidTransactionKey() throws Exception {
		try ( CloseableHttpResponse response =
					  http.getForEntity(get("/changePassword")
							  .addParameter("key", "bogus")
							  .addParameter("changePassword", "Longh0rn#p@cifiers!")
							  .addParameter("verifyChangePassword", "Longh0rn#p@cifiers!")) ) {

			logout();
			String content = login("user", "password");

			Assert.assertTrue(content.contains("Welcome, User"));
			Assert.assertFalse(content.contains("password is incorrect"));
		}
	}
}
