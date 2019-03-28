package jaicore.CustomDataTypes;

/**
 * @author Helen Beierling
 *
 * @param <S> stands for the observed solution
 */
public class Solution<S> {
	private S solution;
	
	public Solution() {}
	public Solution(S solu) {
		this.solution = solu;
	}
	
	public S getSolution() {
		return solution;
	}
	public void setSolution(S newsolution) {
		this.solution = newsolution;
	}
	public boolean isEmpty() {
		return solution != null;
	}

}
