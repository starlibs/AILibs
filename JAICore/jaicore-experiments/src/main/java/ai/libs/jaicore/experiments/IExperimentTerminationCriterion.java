package ai.libs.jaicore.experiments;

import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.events.AlgorithmEvent;

/**
 * Decides, based on a new incoming event, whether the experiment should be stopped.
 *
 * @author felix
 *
 */
public interface IExperimentTerminationCriterion {
	public boolean doesTerminate(AlgorithmEvent e, IAlgorithm<?, ?> algorithm);
}