package hasco.gui.statsplugin;

import hasco.events.HASCOSolutionEvent;
import jaicore.basic.algorithm.events.serializable.PropertyProcessedAlgorithmEvent;
import jaicore.graphvisualizer.plugin.ASimpleMVCPluginController;
import jaicore.graphvisualizer.plugin.solutionperformanceplotter.ScoredSolutionCandidateInfo;
import jaicore.graphvisualizer.plugin.solutionperformanceplotter.ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer;

/**
 * 
 * @author fmohr
 * 
 *         The controller of the HASCOModelStatisticsPlugin
 *
 */
public class HASCOModelStatisticsPluginController extends ASimpleMVCPluginController<HASCOModelStatisticsPluginModel, HASCOModelStatisticsPluginView> {

	public HASCOModelStatisticsPluginController(HASCOModelStatisticsPluginModel model, HASCOModelStatisticsPluginView view) {
		super(model, view);
	}

	@Override
	protected void handleAlgorithmEventInternally(PropertyProcessedAlgorithmEvent algorithmEvent) {
		if (algorithmEvent.correspondsToEventOfClass(HASCOSolutionEvent.class)) {
			Object rawScoredSolutionCandidateInfo = algorithmEvent.getProperty(ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer.SCORED_SOLUTION_CANDIDATE_INFO_PROPERTY_NAME);
			if (rawScoredSolutionCandidateInfo != null) {
				ScoredSolutionCandidateInfo scoredSolutionCandidateInfo = (ScoredSolutionCandidateInfo) rawScoredSolutionCandidateInfo;
				getModel().addEntry(scoredSolutionCandidateInfo);
			}
		}
	}

}
