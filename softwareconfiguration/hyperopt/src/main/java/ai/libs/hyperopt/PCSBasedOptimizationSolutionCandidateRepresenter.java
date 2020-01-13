package ai.libs.hyperopt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import ai.libs.hasco.core.HASCOSolutionCandidate;
import ai.libs.hasco.gui.statsplugin.ComponentInstanceSerializer;
import ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter.SolutionCandidateRepresenter;

public class PCSBasedOptimizationSolutionCandidateRepresenter implements SolutionCandidateRepresenter {

	private static final Logger LOGGER = LoggerFactory.getLogger(PCSBasedOptimizationSolutionCandidateRepresenter.class);

	private ComponentInstanceSerializer componentInstanceSerializer;

	public PCSBasedOptimizationSolutionCandidateRepresenter() {
		this.componentInstanceSerializer = new ComponentInstanceSerializer();
	}

	@Override
	public String getStringRepresentationOfSolutionCandidate(final Object solutionCandidate) {
		if (solutionCandidate instanceof HASCOSolutionCandidate) {
			HASCOSolutionCandidate<?> hascoSolutionCandidate = (HASCOSolutionCandidate<?>) solutionCandidate;
			try {
				return this.componentInstanceSerializer.serializeComponentInstance(hascoSolutionCandidate.getComponentInstance());
			} catch (JsonProcessingException e) {
				LOGGER.error("Cannot compute String representation of solution candidate {} using {}.", solutionCandidate, ComponentInstanceSerializer.class.getSimpleName(), e);
			}
		}
		return solutionCandidate.toString();
	}

}
