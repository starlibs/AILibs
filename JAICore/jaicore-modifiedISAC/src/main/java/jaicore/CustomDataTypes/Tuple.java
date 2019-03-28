package jaicore.CustomDataTypes;

/**
 * Tuple.java - Stores a solution as well as the according performance of that solution
 * 
 * @author Helen Beieling
 *
 * @param <S> A solution
 * @param <P> A performance value
 */
public class Tuple<S,P>{
	private S solution;
	private P performance;
	
	public Tuple(S solu, P perfor){
		this.solution = solu;
		this.performance = perfor;
	}
	
	public S getSolution(){
		return this.solution;
	}
	
	public P getPerformance(){
		return this.performance;
	}
}
