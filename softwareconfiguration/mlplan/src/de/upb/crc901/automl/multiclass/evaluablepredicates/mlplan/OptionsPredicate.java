package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.theories.EvaluablePredicate;

public abstract class OptionsPredicate implements EvaluablePredicate  {

	protected abstract List<? extends Object> getValidValues();
	@Override
	public final boolean test(Monom state, ConstantParam... params) {
		return getValidValues().contains(Integer.valueOf(params[0].getName()));
	}

	@Override
	public final Collection<List<ConstantParam>> getParamsForPositiveEvaluation(Monom state, ConstantParam... partialGrounding) {
		List<List<ConstantParam>> validGroundings = new ArrayList<>();
		for (Object value : getValidValues()) {
			List<ConstantParam> paramList = new ArrayList<>();
			paramList.add(new ConstantParam("" + value.toString()));
			validGroundings.add(paramList);
		}
		return validGroundings;
	}

	public final Collection<List<ConstantParam>> getParamsForNegativeEvaluation(Monom state, ConstantParam... partialGrounding) {
		return null;
	}

	@Override
	public final boolean isOracable() {
		return true;
	}

}
