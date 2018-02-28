package de.upb.crc901.mlplan.evaluablepredicates.mlplan.MLPClassifier;

import java.util.Arrays;
import java.util.List;

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;

public class OptionsFor_MLPClassifier_activation extends OptionsPredicate {
	
	private static List<Integer> validValues = Arrays.asList(new Integer[]{1, 2, 3});

	@Override
	protected List<? extends Object> getValidValues() {
		return validValues;
	}
}

