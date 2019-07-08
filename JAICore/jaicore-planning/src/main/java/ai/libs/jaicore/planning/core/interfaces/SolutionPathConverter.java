package ai.libs.jaicore.planning.core.interfaces;
import java.util.List;

import ai.libs.jaicore.planning.core.Action;

public interface SolutionPathConverter<T> {
	public T convertPathToSolution(List<Action> path);
}
