package hasco.examples;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import hasco.core.HASCOSolutionCandidate;
import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import hasco.observers.HASCOModelStatisticsObserver;
import hasco.serialization.UnresolvableRequiredInterfaceException;
import hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirst;
import hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirstFactory;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.exceptions.AlgorithmException;

public class HASCOModelStatisticsObserverPluginExample {
	public static void main(String[] args) throws UnresolvableRequiredInterfaceException, IOException, InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		HASCOViaFDAndBestFirstFactory<Double> hascoFactory = new HASCOViaFDAndBestFirstFactory<>(n -> 0.0);
		hascoFactory.setProblemInput(new RefinementConfiguredSoftwareConfigurationProblem<>(new File("testrsc/difficultproblem.json"), "IFace", n -> System.currentTimeMillis() * 1.0));
		HASCOViaFDAndBestFirst<Double> hasco = hascoFactory.getAlgorithm();
		hasco.setNumCPUs(1);
		HASCOModelStatisticsObserver observer = new HASCOModelStatisticsObserver();
		hasco.registerListener(observer);
		hasco.setVisualization(true);
		HASCOSolutionCandidate<Double> solution = hasco.call();
		System.out.println(observer.getPerformanceStatisticsPerComposition());
	}
}
