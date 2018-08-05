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
package com.joshcummings.codeplay.terracotta.service.passwords;

import java.util.ArrayList;
import java.util.List;

import me.gosimple.nbvcxz.Nbvcxz;
import me.gosimple.nbvcxz.resources.Configuration;
import me.gosimple.nbvcxz.resources.ConfigurationBuilder;
import me.gosimple.nbvcxz.resources.Feedback;
import me.gosimple.nbvcxz.scoring.Result;
import me.gosimple.nbvcxz.scoring.TimeEstimate;

public class NbvcxzPasswordEntropyEvaluator implements PasswordEntropyEvaluator {
	private final Configuration configuration =
			new ConfigurationBuilder().setMinimumEntropy(40d).createConfiguration();

	private final Nbvcxz evaluator = new Nbvcxz(configuration);

	@Override
	public Evaluation evaluate(String password) {
		Result result = evaluator.estimate(password);
		Feedback feedback = result.getFeedback();
		if ( result.isMinimumEntropyMet() ) {
			return Evaluation.success();
		} else {
			List<String> details = new ArrayList<>();
			details.addAll(feedback.getSuggestion());
			details.add(feedback.getWarning());
			details.add("Your password would be cracked in " +
					TimeEstimate.getTimeToCrackFormatted(result, "OFFLINE_BCRYPT_10"));
			return Evaluation.failure(details);
		}
	}
}
