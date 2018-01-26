package de.upb.crc901.mlplan.evaluablepredicates;

import java.util.Collection;
import java.util.List;

import de.upb.crc901.mlplan.core.MLUtil;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.graphgenerators.task.ceociptfd.EvaluablePredicate;

public class ContainsPredicate implements EvaluablePredicate {

	@Override
	public boolean test(Monom state, ConstantParam... params) {
		String cluster = params[1].getName();
		List<String> itemsInSet = MLUtil.getObjectsInSet(state, cluster);
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
