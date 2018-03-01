package de.upb.crc901.mlplan.evaluablepredicates.mlplan.LogisticRegressionCV;

import java.util.Arrays;
import java.util.List;

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
/*
    solver : {'newton-cg', 'lbfgs', 'liblinear', 'sag', 'saga'},
        default: 'lbfgs'
        Algorithm to use in the optimization problem.

        - For small datasets, 'liblinear' is a good choice, whereas 'sag' and
            'saga' are faster for large ones.
        - For multiclass problems, only 'newton-cg', 'sag', 'saga' and 'lbfgs'
            handle multinomial loss; 'liblinear' is limited to one-versus-rest
            schemes.
        - 'newton-cg', 'lbfgs' and 'sag' only handle L2 penalty, whereas
            'liblinear' and 'saga' handle L1 penalty.
        - 'liblinear' might be slower in LogisticRegressionCV because it does
            not handle warm-starting.

        Note that 'sag' and 'saga' fast convergence is only guaranteed on
        features with approximately the same scale. You can preprocess the data
        with a scaler from sklearn.preprocessing.

        .. versionadded:: 0.17
           Stochastic Average Gradient descent solver.
        .. versionadded:: 0.19
           SAGA solver.


 */
public class OptionsFor_LogisticRegressionCV_solver extends OptionsPredicate {
	
	private static List<Integer> validValues = Arrays.asList(new Integer[]{1, 2, 3});

	@Override
	protected List<? extends Object> getValidValues() {
		return validValues;
	}
}

