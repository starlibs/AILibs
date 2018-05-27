package jaicore.planning.graphgenerators.task.ceociptfd;

import java.util.Collection;
import java.util.List;

import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.model.core.Action;

public interface OracleTaskResolver {
	
	public Collection<List<Action>> getSubSolutions(Monom state, Literal task) throws Exception;
}
