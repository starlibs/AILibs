package jaicore.graphvisualizer.plugin.solutionperformanceplotter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.events.ScoredSolutionCandidateFoundEvent;
import jaicore.basic.algorithm.events.serializable.PropertyProcessedAlgorithmEvent;
import jaicore.graphvisualizer.events.gui.GUIEvent;
import jaicore.graphvisualizer.plugin.ASimpleMVCPluginController;
import jaicore.graphvisualizer.plugin.controlbar.ResetEvent;
import jaicore.graphvisualizer.plugin.timeslider.GoToTimeStepEvent;

public class SolutionPerformanceTimelinePluginController extends ASimpleMVCPluginController<SolutionPerformanceTimelinePluginModel, SolutionPerformanceTimelinePluginView> {

	private Logger logger = LoggerFactory.getLogger(SolutionPerformanceTimelinePlugin.class);

	public SolutionPerformanceTimelinePluginController(SolutionPerformanceTimelinePluginModel model, SolutionPerformanceTimelinePluginView view) {
		super(model, view);
	}

	@Override
	public void handleGUIEvent(GUIEvent guiEvent) {
		if (guiEvent instanceof ResetEvent || guiEvent instanceof GoToTimeStepEvent) {
			getModel().clear();
		}
	}

	@Override
	public void handleAlgorithmEventInternally(PropertyProcessedAlgorithmEvent algorithmEvent) {
		if (algorithmEvent.correspondsToEventOfClass(ScoredSolutionCandidateFoundEvent.class)) {

			logger.debug("Received solution event {}", algorithmEvent);

			Object rawScoredSolutionCandidateInfo = algorithmEvent.getProperty(ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer.SCORED_SOLUTION_CANDIDATE_INFO_PROPERTY_NAME);
			if (rawScoredSolutionCandidateInfo != null) {
				ScoredSolutionCandidateInfo scoredSolutionCandidateInfo = (ScoredSolutionCandidateInfo) rawScoredSolutionCandidateInfo;

				try {
					logger.debug("Adding solution to model and updating view.");
					getModel().addEntry(algorithmEvent.getTimestampOfEvent(), parseScoreToDouble(scoredSolutionCandidateInfo.getScore()));
					logger.debug("Added solution to model.");
				} catch (NumberFormatException exception) {
					logger.warn("Received processed SolutionCandidateFoundEvent, but the score {} cannot be parsed to a double.", scoredSolutionCandidateInfo.getScore());
					return;
				}
			}
		}
	}

	private double parseScoreToDouble(String score) throws NumberFormatException {
		return Double.parseDouble(score);
	}

}
