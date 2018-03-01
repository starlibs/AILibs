package de.upb.crc901.mlplan.evaluablepredicates.mlplan.MLPClassifier;

import java.util.Arrays;
import java.util.List;

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
/*
    solver : {'lbfgs', 'sgd', 'adam'}, default 'adam'
        The solver for weight optimization.

        - 'lbfgs' is an optimizer in the family of quasi-Newton methods.

        - 'sgd' refers to stochastic gradient descent.

        - 'adam' refers to a stochastic gradient-based optimizer proposed
          by Kingma, Diederik, and Jimmy Ba

        Note: The default solver 'adam' works pretty well on relatively
        large datasets (with thousands of training samples or more) in terms of
        both training time and validation score.
        For small datasets, however, 'lbfgs' can converge faster and perform
        better.


 */
public class OptionsFor_MLPClassifier_solver extends OptionsPredicate {
	
	private static List<Object> validValues = Arrays.asList(new Object[]{});

	@Override
	protected List<? extends Object> getValidValues() {
		return validValues;
	}
}

