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

public class PartitionPredicate implements EvaluablePredicate {

	@Override
	public boolean test(Monom state, ConstantParam... params) {
		List<String> union = MLUtil.getObjectsInSet(state, params[0].getName());
		List<String> p1 = MLUtil.getObjectsInSet(state, params[1].getName());
		List<String> p2 = MLUtil.getObjectsInSet(state, params[2].getName());
		System.out.println(union);
		System.out.println(p1);
		System.out.println(p2);
		return false;
	}

	@Override
	public Collection<List<ConstantParam>> getParamsForPositiveEvaluation(Monom state, ConstantParam... partialGrounding) {
		return null;
	}

	public Collection<List<ConstantParam>> getParamsForNegativeEvaluation(Monom state, ConstantParam... partialGrounding) {
		return null;
	}

	@Override
	public boolean isOracable() {
		return false;
	}
}
