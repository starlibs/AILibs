package ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.ceociptfd;

import java.util.Collection;
import java.util.List;

import ai.libs.jaicore.logic.fol.structure.Literal;
import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.planning.core.Action;

public interface OracleTaskResolver {
	
	public Collection<List<Action>> getSubSolutions(Monom state, Literal task) throws Exception;
}
