package jaicore.planning.model.core;
import java.util.List;

public interface SolutionPathConverter<T> {
	public T convertPathToSolution(List<Action> path);
}
