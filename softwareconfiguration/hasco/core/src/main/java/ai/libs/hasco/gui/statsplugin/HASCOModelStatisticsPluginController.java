package ai.libs.hasco.gui.statsplugin;

import org.api4.java.algorithm.events.serializable.IPropertyProcessedAlgorithmEvent;

import ai.libs.hasco.events.HASCOSolutionEvent;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginController;
import ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter.ScoredSolutionCandidateInfo;
import ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter.ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer;

/**
 *
 * @author fmohr
 *
 *         The controller of the HASCOModelStatisticsPlugin
 *
 */
public class HASCOModelStatisticsPluginController extends ASimpleMVCPluginController<HASCOModelStatisticsPluginModel, HASCOModelStatisticsPluginView> {

	public HASCOModelStatisticsPluginController(final HASCOModelStatisticsPluginModel model, final HASCOModelStatisticsPluginView view) {
		super(model, view);
	}

	@Override
	protected void handleAlgorithmEventInternally(final IPropertyProcessedAlgorithmEvent algorithmEvent) {
		if (algorithmEvent.correspondsToEventOfClass(HASCOSolutionEvent.class)) {
			Object rawScoredSolutionCandidateInfo = algorithmEvent.getProperty(ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer.SCORED_SOLUTION_CANDIDATE_INFO_PROPERTY_NAME);
			if (rawScoredSolutionCandidateInfo != null) {
				ScoredSolutionCandidateInfo scoredSolutionCandidateInfo = (ScoredSolutionCandidateInfo) rawScoredSolutionCandidateInfo;
				this.getModel().addEntry(scoredSolutionCandidateInfo);
			}
		}
	}

}
