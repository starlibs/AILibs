package hasco.gui.statsplugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import hasco.core.HASCOSolutionCandidate;
import jaicore.graphvisualizer.plugin.solutionperformanceplotter.SolutionCandidateRepresenter;

public class HASCOSolutionCandidateRepresenter implements SolutionCandidateRepresenter {

	private static final Logger LOGGER = LoggerFactory.getLogger(HASCOSolutionCandidateRepresenter.class);

	private ComponentInstanceSerializer componentInstanceSerializer;

	public HASCOSolutionCandidateRepresenter() {
		componentInstanceSerializer = new ComponentInstanceSerializer();
	}

	@Override
	public String getStringRepresentationOfSolutionCandidate(Object solutionCandidate) {
		if (solutionCandidate instanceof HASCOSolutionCandidate) {
			HASCOSolutionCandidate<?> hascoSolutionCandidate = (HASCOSolutionCandidate<?>) solutionCandidate;
			try {
				String serializedComponentInstance = componentInstanceSerializer.serializeComponentInstance(hascoSolutionCandidate.getComponentInstance());
				return serializedComponentInstance;
			} catch (JsonProcessingException e) {
				LOGGER.error("Cannot compute String representation of solution candidate {} using {}.", solutionCandidate, ComponentInstanceSerializer.class.getSimpleName(), e);
			}
		}
		return solutionCandidate.toString();
	}

}
