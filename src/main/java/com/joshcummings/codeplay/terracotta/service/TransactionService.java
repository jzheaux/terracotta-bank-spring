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

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.joshcummings.codeplay.terracotta.model.Transaction;
import com.joshcummings.codeplay.terracotta.model.User;

import org.springframework.stereotype.Service;

/**
 * This service makes Terracotta vulnerable to DoS and Transaction
 * Hijacking
 *
 * It is vulnerable to DoS because there is neither an expiry nor
 * a cap on the {@code transactions} cache.
 *
 * It is vulnerable to Transaction Hijacking because the key is guessable.
 *
 * It is also vulnerable to hijacking because the incrementer is not atomic
 * and in the even of a lost update, two people could get the same
 * key.
 *
 * @author Josh Cummings
 */
@Service
public class TransactionService {
	private final Cache<String, Transaction> transactions =
			CacheBuilder.newBuilder()
				.expireAfterWrite(2, TimeUnit.DAYS)
				.maximumSize(10000)
				.build();

	private final SecureRandom rand = new SecureRandom();

	public Transaction beginTransaction(User user, String action) {
		String key = String.valueOf(nextId());
		Transaction transaction = new Transaction(key, user, action);
		this.transactions.put(key, transaction);
		return transaction;
	}

	public Transaction retrieveTransaction(String key) {
		return this.transactions.getIfPresent(key);
	}

	public void endTransaction(String key) {
		this.transactions.invalidate(key);
	}

	public Collection<Transaction> retrieveTransactionsForUser(User user) {
		Collection<Transaction> userTransactions = new ArrayList<>();
		for ( Map.Entry<String, Transaction> entry : this.transactions.asMap().entrySet() ) {
			User transactionUser = this.transactions.getIfPresent(entry.getKey()).getUser();
			if ( transactionUser.equals(user.getId()) ) {
				userTransactions.add(entry.getValue());
			}
		}
		return userTransactions;
	}

	public void endAllTransactionsForUser(User user) {
		for ( Map.Entry<String, Transaction> entry : this.transactions.asMap().entrySet() ) {
			User transactionUser = retrieveTransaction(entry.getKey()).getUser();
			if ( transactionUser.equals(user.getId()) ) {
				endTransaction(entry.getKey());
			}
		}
	}

	private String nextId() {
		byte[] bytes = new byte[16];
		this.rand.nextBytes(bytes);
		return new BigInteger(bytes).toString(16);
	}
}
