package de.upb.crc901.mlplan.evaluablepredicates;

import java.util.Collection;
import java.util.List;

import de.upb.crc901.mlplan.core.MLUtil;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.graphgenerators.task.ceociptfd.EvaluablePredicate;

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
		return MLUtil.getObjectsInSet(state, params[0].getName()).size() == 2;
	}

}
