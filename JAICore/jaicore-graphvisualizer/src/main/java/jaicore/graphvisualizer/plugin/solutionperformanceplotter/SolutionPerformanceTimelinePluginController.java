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

	@SuppressWarnings("unchecked")
	@Override
	public void handleAlgorithmEventInternally(PropertyProcessedAlgorithmEvent algorithmEvent) {
		if (algorithmEvent instanceof ScoredSolutionCandidateFoundEvent) {
			logger.debug("Received solution event {}", algorithmEvent);
			ScoredSolutionCandidateFoundEvent<?, ?> event = (ScoredSolutionCandidateFoundEvent<?, ?>) algorithmEvent;
			if (!(event.getScore() instanceof Number)) {
				logger.warn("Received SolutionCandidateFoundEvent, but the score is of type {}, which is not a number.", event.getScore().getClass().getName());
				return;
			}
			logger.debug("Adding solution to model and updating view.");
			getModel().addEntry((ScoredSolutionCandidateFoundEvent<?, ? extends Number>) event);
		} else {
			logger.trace("Received and ignored irrelevant event {}", algorithmEvent);
		}
	}

}
