package de.upb.crc901.mlplan.evaluablepredicates.mlplan.PassiveAggressiveClassifier;

import java.util.Arrays;
import java.util.List;

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
/*
    loss : string, optional
        The loss function to be used:
        hinge: equivalent to PA-I in the reference paper.
        squared_hinge: equivalent to PA-II in the reference paper.


 */
public class OptionsFor_PassiveAggressiveClassifier_loss extends OptionsPredicate {
	
	private static List<Object> validValues = Arrays.asList(new Object[]{});

	@Override
	protected List<? extends Object> getValidValues() {
		return validValues;
	}
}

