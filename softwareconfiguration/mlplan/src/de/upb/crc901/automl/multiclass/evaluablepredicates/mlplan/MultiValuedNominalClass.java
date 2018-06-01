package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.theories.EvaluablePredicate;

public class MultiValuedNominalClass implements EvaluablePredicate  {

	
	@Override
	public final boolean test(Monom state, ConstantParam... params) {
		String nameOfDataset = params[0].getName();
		Optional<Literal> relationThatDeclaresSetOfClasses = state.getLiteralsWithPropertyName("definesClassesOf").stream().filter(l -> l.getParameters().get(1).getName().equals(nameOfDataset)).findFirst();
		if (!relationThatDeclaresSetOfClasses.isPresent())
			return false;
		String nameOfClassContainer = relationThatDeclaresSetOfClasses.get().getParameters().get(0).getName();
		Collection<Literal> membershipPredicates = state.getLiteralsWithPropertyName("in").stream().filter(l -> l.getParameters().get(1).getName().equals(nameOfClassContainer)).collect(Collectors.toList());
		return membershipPredicates.size() > 2; 
	}

	@Override
	public final Collection<List<ConstantParam>> getParamsForPositiveEvaluation(Monom state, ConstantParam... partialGrounding) {
		throw new UnsupportedOperationException("This is not an oraclable predicate!");
	}

	public final Collection<List<ConstantParam>> getParamsForNegativeEvaluation(Monom state, ConstantParam... partialGrounding) {
		throw new UnsupportedOperationException("This is not an oraclable predicate!");
	}

	@Override
	public final boolean isOracable() {
		return false;
	}

}
