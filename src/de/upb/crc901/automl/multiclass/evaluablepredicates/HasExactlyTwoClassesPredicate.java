package de.upb.crc901.automl.multiclass.evaluablepredicates;

import java.util.Collection;
import java.util.List;

import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.theories.EvaluablePredicate;
import jaicore.logic.fol.theories.set.SetTheoryUtil;

public class HasExactlyTwoClassesPredicate implements EvaluablePredicate {

	@Override
	public Collection<List<ConstantParam>> getParamsForPositiveEvaluation(Monom state, ConstantParam... partialGrounding) {
		return null;
	}

	@Override
	public boolean isOracable() {
		return false;
	}

	@Override
	public Collection<List<ConstantParam>> getParamsForNegativeEvaluation(Monom state, ConstantParam... partialGrounding) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean test(Monom state, ConstantParam... params) {
		return SetTheoryUtil.getObjectsInSet(state, params[0].getName()).size() == 2;
	}

}
