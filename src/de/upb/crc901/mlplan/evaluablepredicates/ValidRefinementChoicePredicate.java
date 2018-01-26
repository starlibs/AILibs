package de.upb.crc901.mlplan.evaluablepredicates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.upb.crc901.mlplan.core.MLUtil;
import jaicore.basic.SetUtil;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.graphgenerators.task.ceociptfd.EvaluablePredicate;

public class ValidRefinementChoicePredicate implements EvaluablePredicate {

	@Override
	public boolean test(Monom state, ConstantParam... params) {
		return false;
	}

	@Override
	public Collection<List<ConstantParam>> getParamsForPositiveEvaluation(Monom state, ConstantParam... partialGrounding) {
		ConstantParam sup = partialGrounding[1];
		List<String> set = MLUtil.getObjectsInSet(state, sup.getName());
		Collection<List<ConstantParam>> subsets = new ArrayList<>();
		if (set.isEmpty())
			return subsets;

		String minItem = set.stream().min((s1, s2) -> s1.compareTo(s2)).get();
		try {
			for (Collection<String> subset : SetUtil.powerset(set)) {
				if (!subset.isEmpty() && subset.size() != set.size() && subset.contains(minItem))
					subsets.add(Arrays.asList(new ConstantParam[] { new ConstantParam(SetUtil.serializeAsSet(subset)), sup }));
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return subsets;
	}

	public Collection<List<ConstantParam>> getParamsForNegativeEvaluation(Monom state, ConstantParam... partialGrounding) {
		return null;
	}

	@Override
	public boolean isOracable() {
		return true;
	}
}
