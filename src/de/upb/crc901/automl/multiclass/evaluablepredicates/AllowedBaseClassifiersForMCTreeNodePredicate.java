package de.upb.crc901.automl.multiclass.evaluablepredicates;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.theories.EvaluablePredicate;

public class AllowedBaseClassifiersForMCTreeNodePredicate implements EvaluablePredicate {

	List<String> allowedClassifiers = Arrays.asList(new String[] {"weka.classifiers.trees.J48", "weka.classifiers.functions.SMO", "weka.classifiers.functions.Logistic", "weka.classifiers.trees.RandomForest"});
	
	@Override
	public boolean test(Monom state, ConstantParam... params) {
		return allowedClassifiers.contains(params[0].getName());
	}

	@Override
	public Collection<List<ConstantParam>> getParamsForPositiveEvaluation(Monom state, ConstantParam... partialGrounding) {
		Collection<List<ConstantParam>> params = allowedClassifiers.stream().map(n -> Arrays.asList(new ConstantParam[] {new ConstantParam("\"" + n + "\"")})).collect(Collectors.toList());
		return params;
	}

	public Collection<List<ConstantParam>> getParamsForNegativeEvaluation(Monom state, ConstantParam... partialGrounding) {
		return null;
	}

	@Override
	public boolean isOracable() {
		return true;
	}
}
