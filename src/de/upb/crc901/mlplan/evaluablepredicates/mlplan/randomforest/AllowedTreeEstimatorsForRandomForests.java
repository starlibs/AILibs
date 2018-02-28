package de.upb.crc901.mlplan.evaluablepredicates.mlplan.randomforest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.graphgenerators.task.ceociptfd.EvaluablePredicate;

public class AllowedTreeEstimatorsForRandomForests extends OptionsPredicate {

	private static List<Integer> validValues = Arrays.asList(new Integer[]{5, 10, 50, 100, 200, 500});

	@Override
	protected List<? extends Object> getValidValues() {
		return validValues;
	}
	
}
