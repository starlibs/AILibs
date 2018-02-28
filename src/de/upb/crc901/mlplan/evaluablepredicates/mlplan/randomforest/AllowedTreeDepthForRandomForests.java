package de.upb.crc901.mlplan.evaluablepredicates.mlplan.randomforest;

import java.util.Arrays;
import java.util.List;

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;

public class AllowedTreeDepthForRandomForests extends OptionsPredicate {
	
	private static List<Integer> validValues = Arrays.asList(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});

	@Override
	protected List<? extends Object> getValidValues() {
		return validValues;
	}
}
