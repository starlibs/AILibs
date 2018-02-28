package de.upb.crc901.mlplan.evaluablepredicates.mlplan.randomforest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.graphgenerators.task.ceociptfd.EvaluablePredicate;

public class AllowedTreeDepthForRandomForests implements EvaluablePredicate {
	
	private static List<Integer> validValues = Arrays.asList(new Integer[]{1, 2, 3, 4, 5});

	@Override
	public boolean test(Monom state, ConstantParam... params) {
		return validValues.contains(Integer.valueOf(params[0].getName()));
	}

	@Override
	public Collection<List<ConstantParam>> getParamsForPositiveEvaluation(Monom state, ConstantParam... partialGrounding) {
		List<List<ConstantParam>> validGroundings = new ArrayList<>();
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
