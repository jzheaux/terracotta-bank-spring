package com.joshcummings.codeplay.terracotta.service;

import java.util.regex.Pattern;

public class WeakPasswordComplexityEvaluator implements PasswordComplexityEvaluator {
	private static final String SPECIAL_CHARACTERS = "!@#^";

	private static final Pattern HAS_LOWER = Pattern.compile("[a-z]");
	private static final Pattern HAS_UPPER = Pattern.compile("[A-Z]");
	private static final Pattern HAS_NUMBER = Pattern.compile("[0-9]");
	private static final Pattern HAS_SPECIAL = Pattern.compile("[" + SPECIAL_CHARACTERS + "]");
	private static final Pattern HAS_ONLY = Pattern.compile("[a-zA-Z0-9" + SPECIAL_CHARACTERS + "]{6,20}");

	@Override
	public boolean evaluate(String password) {
		return HAS_LOWER.matcher(password).find() &&
				HAS_UPPER.matcher(password).find() &&
				HAS_NUMBER.matcher(password).find() &&
				HAS_SPECIAL.matcher(password).find() &&
				HAS_ONLY.matcher(password).matches();
	}
}
