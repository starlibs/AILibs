package jaicore.planning.algorithms.strips;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.planning.EvaluatedPlan;
import jaicore.planning.algorithms.IPlanningAlgorithm;
import jaicore.planning.model.strips.StripsAction;
import jaicore.planning.model.strips.StripsPlanningProblem;

public class STRIPSPlanner<V extends Comparable<V>> implements IPlanningAlgorithm<StripsPlanningProblem, EvaluatedPlan<StripsAction, V>> {
	private final StripsPlanningProblem problem;
	
	public STRIPSPlanner(StripsPlanningProblem problem) {
		super();
		this.problem = problem;
	}

	@Override
	public StripsPlanningProblem getInput() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void registerListener(Object listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setNumCPUs(int numberOfCPUs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getNumCPUs() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setTimeout(int timeout, TimeUnit timeUnit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public TimeUnit getTimeoutUnit() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AlgorithmEvent nextWithException() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<AlgorithmEvent> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AlgorithmEvent next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EvaluatedPlan<StripsAction, V> call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, Object> getAnnotationsOfSolution(EvaluatedPlan<StripsAction, V> solution) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
