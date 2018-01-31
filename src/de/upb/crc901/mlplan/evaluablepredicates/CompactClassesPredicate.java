package de.upb.crc901.mlplan.evaluablepredicates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.upb.crc901.mlplan.core.MLUtil;
import jaicore.basic.SetUtil;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.graphgenerators.task.ceociptfd.EvaluablePredicate;

public class CompactClassesPredicate implements EvaluablePredicate {

	@Override
	public boolean test(Monom state, ConstantParam... params) {
		Set<String> p1 = new HashSet<>(MLUtil.getObjectsInSet(state, params[0].getName()));
		Set<String> p2 = new HashSet<>(MLUtil.getObjectsInSet(state, params[1].getName()));
		return p1.equals(p2);
	}

	@Override
	public Collection<List<ConstantParam>> getParamsForPositiveEvaluation(Monom state, ConstantParam... partialGrounding) {
		List<String> p1 = MLUtil.getObjectsInSet(state, partialGrounding[0].getName());
		Collection<List<ConstantParam>> validGroundings = new ArrayList<>();
		validGroundings.add(Arrays.asList(new ConstantParam[] { partialGrounding[0], new ConstantParam(SetUtil.serializeAsSet(p1)) }));
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
