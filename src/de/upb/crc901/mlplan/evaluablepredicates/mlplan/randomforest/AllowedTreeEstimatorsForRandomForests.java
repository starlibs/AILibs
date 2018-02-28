package de.upb.crc901.mlplan.evaluablepredicates.mlplan.randomforest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.graphgenerators.task.ceociptfd.EvaluablePredicate;

public class AllowedTreeEstimatorsForRandomForests implements EvaluablePredicate {

	@Override
	public boolean test(Monom state, ConstantParam... params) {
		return false;
	}

	@Override
	public Collection<List<ConstantParam>> getParamsForPositiveEvaluation(Monom state, ConstantParam... partialGrounding) {
		List<List<ConstantParam>> validGroundings = new ArrayList<>();
		int[] validValues = new int[]{5, 10, 50, 100, 200, 500};
		for (int value : validValues) {
			List<ConstantParam> paramList = new ArrayList<>();
			paramList.add(new ConstantParam("" + value));
			validGroundings.add(paramList);
		}
		return validGroundings;
	}

	public Collection<List<ConstantParam>> getParamsForNegativeEvaluation(Monom state, ConstantParam... partialGrounding) {
		return null;
	}

	@Override
	public boolean isOracable() {
		return true;
	}
}
