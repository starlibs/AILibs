package jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.ceociptfd;

import java.util.Collection;
import java.util.List;

import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.core.Action;

public interface OracleTaskResolver {
	
	public Collection<List<Action>> getSubSolutions(Monom state, Literal task) throws Exception;
}
