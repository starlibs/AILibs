package de.upb.crc901.mlplan.evaluablepredicates.mlplan.LinearSVC;

import java.util.Arrays;
import java.util.List;

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
/*
    loss : string, 'hinge' or 'squared_hinge' (default='squared_hinge')
        Specifies the loss function. 'hinge' is the standard SVM loss
        (used e.g. by the SVC class) while 'squared_hinge' is the
        square of the hinge loss.


 */
public class OptionsFor_LinearSVC_loss extends OptionsPredicate {
	
	private static List<Object> validValues = Arrays.asList(new Object[]{});

	@Override
	protected List<? extends Object> getValidValues() {
		return validValues;
	}
}

