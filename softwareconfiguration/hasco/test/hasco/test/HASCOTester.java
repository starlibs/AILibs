package hasco.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import hasco.core.HASCO;
import hasco.core.HASCOFactory;
import hasco.core.HASCORunReport;
import hasco.core.HASCOSolutionCandidate;
import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import hasco.events.HASCOSolutionEvent;
import hasco.model.ComponentInstance;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.basic.algorithm.GeneralAlgorithmTester;

public abstract class HASCOTester<ISearch, N, A> extends GeneralAlgorithmTester<RefinementConfiguredSoftwareConfigurationProblem<Double>, RefinementConfiguredSoftwareConfigurationProblem<Double>, HASCORunReport<Double>> {

	@Test
	public void testThatAnEventForEachPossibleSolutionIsEmittedInSimpleCall() throws Exception {
		
		/* we check this only for the simple problem here */
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem = getSimpleProblemInputForGeneralTestPurposes();
		HASCOFactory<ISearch, N, A, Double> factory = getFactory();
		factory.setProblemInput(problem);
		HASCO<ISearch, N, A, Double> hasco = factory.getAlgorithm();
		List<ComponentInstance> solutions = new ArrayList<>();
		for (AlgorithmEvent e : hasco) {
			if (e instanceof HASCOSolutionEvent) {
				solutions.add(((HASCOSolutionCandidate<Double>)((HASCOSolutionEvent) e).getSolutionCandidate()).getComponentInstance());
			}
		}
		Set<Object> uniqueSolutions = new HashSet<>(solutions);
		assertEquals("Only found " + uniqueSolutions.size() + "/6 solutions", 6, uniqueSolutions.size());
		assertEquals("All 6 solutions were found, but " + solutions.size() + " solutions were returned in total, i.e. there are solutions returned twice", 6, solutions.size());
	};

	@Test
	public void testThatAnEventForEachPossibleSolutionIsEmittedInParallelizedCall() throws Exception {
		
	};

	@Test
	public void testThatIteratorReturnsEachPossibleSolution() throws Exception {
		
	}
	
	public abstract HASCOFactory<ISearch, N, A, Double> getFactory();

	@Override
	public AlgorithmProblemTransformer<RefinementConfiguredSoftwareConfigurationProblem<Double>, RefinementConfiguredSoftwareConfigurationProblem<Double>> getProblemReducer() {
		return p -> p;
	}

	@Override
	public RefinementConfiguredSoftwareConfigurationProblem<Double> getSimpleProblemInputForGeneralTestPurposes() throws Exception {
		return new RefinementConfiguredSoftwareConfigurationProblem<>(new File("testrsc/simpleproblem.json"), "IFace", n -> 0.0);
	}

	@Override
	public RefinementConfiguredSoftwareConfigurationProblem<Double> getDifficultProblemInputForGeneralTestPurposes() throws IOException {
		return new RefinementConfiguredSoftwareConfigurationProblem<>(new File("testrsc/difficultproblem.json"), "IFace", n -> 0.0);
	};
}
