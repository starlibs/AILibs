package ai.libs.jaicore.experiments;

import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.events.IAlgorithmEvent;

/**
 * Decides, based on a new incoming event, whether the experiment should be stopped.
 *
 * @author Felix Mohr
 *
 */
public interface IExperimentTerminationCriterion {
	public boolean doesTerminate(IAlgorithmEvent e, IAlgorithm<?, ?> algorithm);
}
