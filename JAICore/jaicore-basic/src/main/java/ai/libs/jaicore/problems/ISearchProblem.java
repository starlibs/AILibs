package ai.libs.jaicore.problems;


public interface ISearchProblem<P, S> {

	public P getInstance();

	public boolean isSolution(S candidate);
}
