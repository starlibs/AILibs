package ai.libs.jaicore.logic.fol.theories.set;

import java.util.Collection;
import java.util.List;

import ai.libs.jaicore.logic.fol.structure.ConstantParam;
import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.logic.fol.theories.EvaluablePredicate;

public class ContainsPredicate implements EvaluablePredicate {

	@Override
	public boolean test(Monom state, ConstantParam... params) {
		String cluster = params[1].getName();
		List<String> itemsInSet = SetTheoryUtil.getObjectsInSet(state, cluster);
		return itemsInSet.contains(params[0].getName());
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
