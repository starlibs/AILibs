package jaicore.planning.graphgenerators.task.ceociptfd;

import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Monom;

public interface EvaluablePredicate {
	public boolean test(Monom state, ConstantParam... params); // usually we would evaluate ONLY the predicate with terms, but there may be terms that are described indirectly in the state.
}
