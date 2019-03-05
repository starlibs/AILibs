package jaicore.planning.core;
import java.util.List;

public interface SolutionPathConverter<T> {
	public T convertPathToSolution(List<Action> path);
}
