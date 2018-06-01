package jaicore.logic.fol.theories.set;

import java.util.Collection;
import java.util.List;

import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.theories.EvaluablePredicate;

public class NotEmptyPredicate implements EvaluablePredicate {
	@Override
	public boolean test(Monom state, ConstantParam... params) {
		String cluster = params[0].getName();
		long count = SetTheoryUtil.getObjectsInSet(state, cluster).size();
		return count > 0;
	}

	@Override
	public Collection<List<ConstantParam>> getParamsForPositiveEvaluation(Monom state, ConstantParam... partialGrounding) {
		throw new UnsupportedOperationException("NOT ORACABLE");
	}

	@Override
	public Collection<List<ConstantParam>> getParamsForNegativeEvaluation(Monom state, ConstantParam... partialGrounding) {
		throw new UnsupportedOperationException("NOT ORACABLE");
	}

	@Override
	public boolean isOracable() {
		return false;
	}
}
