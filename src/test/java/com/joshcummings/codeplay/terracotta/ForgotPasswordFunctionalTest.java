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

import java.io.IOException;
import java.util.Collection;

import com.joshcummings.codeplay.terracotta.model.Transaction;
import com.joshcummings.codeplay.terracotta.model.User;
import com.joshcummings.codeplay.terracotta.service.TransactionService;
import com.joshcummings.codeplay.terracotta.service.UserService;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.apache.http.client.methods.RequestBuilder.get;
import static org.apache.http.client.methods.RequestBuilder.post;

public class ForgotPasswordFunctionalTest extends AbstractEmbeddedTomcatTest {

	@Test
	public void testForgotPasswordForEnumeration() {
		String validAccount = http.postForContent(post("/forgotPassword")
			.addParameter("forgotPasswordAccount", "john.coltraine"));
		String invalidAccount = http.postForContent(post("/forgotPassword")
			.addParameter("forgotPasswordAccount", "invalidaccount"));

		Assert.assertEquals(validAccount, invalidAccount);
	}
	
	@Test
	public void testForgotPasswordDoesNotRevealPassword() {
		String validAccount = http.postForContent(post("/forgotPassword")
				.addParameter("forgotPasswordAccount", "john.coltraine"));

		Assert.assertFalse(validAccount.contains("j0hn"));
	}

	@Test
	public void testForgotPasswordCannotBePerformedWithGet() {
		int status = http.getForStatus(get("/forgotPassword")
			.addParameter("forgotPasswordAccount", "john.coltraine"));

		Assert.assertEquals(status, 405);
	}

	@Test
	public void testForgotPasswordUsesTransactionalKeys() throws IOException {
		TransactionService transactionService = this.context.getBean(TransactionService.class);
		UserService userService = this.context.getBean(UserService.class);
		User user = userService.findByUsername("john.coltraine");
		Collection<Transaction> transactions = transactionService.retrieveTransactionsForUser(user);

		Assert.assertTrue(transactions.isEmpty());

		String content = http.postForContent(post("/forgotPassword")
					  		.addParameter("forgotPasswordAccount", "john.coltraine"));

		transactions = transactionService.retrieveTransactionsForUser(user);
		Assert.assertTrue(transactions.size() == 1);
		Assert.assertEquals("change_password", transactions.iterator().next().getAction());
		Assert.assertFalse(content.contains(transactions.iterator().next().getKey()));
	}
}