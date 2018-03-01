package de.upb.crc901.mlplan.evaluablepredicates.mlplan.SVC;

import java.util.Arrays;
import java.util.List;

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
/*
    gamma : float, optional (default='auto')
        Kernel coefficient for 'rbf', 'poly' and 'sigmoid'.
        If gamma is 'auto' then 1/n_features will be used instead.


 */
public class OptionsFor_SVC_gamma extends OptionsPredicate {
	
	private static List<Integer> validValues = Arrays.asList(new Integer[]{1, 2, 3});

	@Override
	protected List<? extends Object> getValidValues() {
		return validValues;
	}
}

