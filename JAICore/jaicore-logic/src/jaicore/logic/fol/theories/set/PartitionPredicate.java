package jaicore.logic.fol.theories.set;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import jaicore.basic.sets.SetUtil;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.theories.EvaluablePredicate;

public class PartitionPredicate implements EvaluablePredicate {

	@Override
	public boolean test(Monom state, ConstantParam... params) {
		List<String> union = SetTheoryUtil.getObjectsInSet(state, params[0].getName());
		List<String> p1 = SetTheoryUtil.getObjectsInSet(state, params[1].getName());
		List<String> p2 = SetTheoryUtil.getObjectsInSet(state, params[2].getName());
		return SetUtil.union(p1,p2).equals(union);
	}

	@Override
	public Collection<List<ConstantParam>> getParamsForPositiveEvaluation(Monom state, ConstantParam... partialGrounding) {
		List<String> union = SetTheoryUtil.getObjectsInSet(state, partialGrounding[0].getName());
		List<String> p1 = partialGrounding[1] != null ? SetTheoryUtil.getObjectsInSet(state, partialGrounding[1].getName()) : null;
		List<String> p2 = partialGrounding[2] != null ? SetTheoryUtil.getObjectsInSet(state, partialGrounding[2].getName()) : null;
		if (p1 == null && p2 == null)
			throw new IllegalArgumentException("At most one of the two last parameters must be null!");
		Collection<List<ConstantParam>> validGroundings = new ArrayList<>();
		if (p1 == null)
			validGroundings.add(Arrays.asList(new ConstantParam[] { partialGrounding[0], new ConstantParam(SetUtil.serializeAsSet(SetUtil.difference(union, p2))), partialGrounding[2] }));
		if (p2 == null)
			validGroundings.add(Arrays.asList(new ConstantParam[] { partialGrounding[0], partialGrounding[1], new ConstantParam(SetUtil.serializeAsSet(SetUtil.difference(union, p1))) }));
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
