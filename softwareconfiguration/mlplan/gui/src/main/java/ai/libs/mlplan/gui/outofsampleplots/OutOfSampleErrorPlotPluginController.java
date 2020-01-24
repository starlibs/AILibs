package ai.libs.mlplan.gui.outofsampleplots;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.ai.ml.classification.IClassifier;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.algorithm.events.serializable.IPropertyProcessedAlgorithmEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ai.libs.jaicore.basic.reconstruction.ReconstructionPlan;
import ai.libs.jaicore.graphvisualizer.events.gui.GUIEvent;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginController;
import ai.libs.jaicore.graphvisualizer.plugin.controlbar.ResetEvent;
import ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter.ScoredSolutionCandidateInfo;
import ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter.ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.timeslider.GoToTimeStepEvent;
import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.core.evaluation.MLEvaluationUtil;
import ai.libs.mlplan.core.events.ClassifierFoundEvent;

public class OutOfSampleErrorPlotPluginController extends ASimpleMVCPluginController<OutOfSampleErrorPlotPluginModel, OutOfSampleErrorPlotPluginView> {

	private ILabeledDataset<?> train, test;
	private Logger logger = LoggerFactory.getLogger(OutOfSampleErrorPlotPlugin.class);

	public OutOfSampleErrorPlotPluginController(final OutOfSampleErrorPlotPluginModel model, final OutOfSampleErrorPlotPluginView view) {
		super(model, view);
	}

	public ILabeledDataset<?> getTrain() {
		return this.train;
	}

	public void setTrain(final ILabeledDataset<?> train) {
		this.train = train;
	}

	public ILabeledDataset<?> getTest() {
		return this.test;
	}

	public void setTest(final ILabeledDataset<?> test) {
		this.test = test;
	}

	@Override
	public void handleGUIEvent(final GUIEvent guiEvent) {
		if (guiEvent instanceof ResetEvent || guiEvent instanceof GoToTimeStepEvent) {
			this.getModel().clear();
		}
	}

	@Override
	public void handleAlgorithmEventInternally(final IPropertyProcessedAlgorithmEvent algorithmEvent) {
		if (algorithmEvent.correspondsToEventOfClass(ClassifierFoundEvent.class)) {
			this.logger.debug("Received classifier found event {}", algorithmEvent);

			Object rawScoredSolutionCandidateInfo = algorithmEvent.getProperty(ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer.SCORED_SOLUTION_CANDIDATE_INFO_PROPERTY_NAME);
			if (rawScoredSolutionCandidateInfo != null) {
				ScoredSolutionCandidateInfo scoredSolutionCandidateInfo = (ScoredSolutionCandidateInfo) rawScoredSolutionCandidateInfo;

				try {
					IClassifier classifier = this.deserializeClassifier(scoredSolutionCandidateInfo.getSolutionCandidateRepresentation());
					this.logger.debug("Building classifier");
					classifier.fit(this.train);
					List<Double> performances = new ArrayList<>();
					performances.add(this.parseScoreToDouble(scoredSolutionCandidateInfo.getScore()));
					performances.add(MLEvaluationUtil.getLossForTrainedClassifier(classifier, this.test, EClassificationPerformanceMeasure.ERRORRATE));
					this.logger.debug("Adding solution to model and updating view.");
					this.getModel().addEntry(algorithmEvent.getTimestampOfEvent(), classifier, performances);
					this.logger.debug("Added solution to model.");
				} catch (NumberFormatException exception) {
					this.logger.warn("Received processed SolutionCandidateFoundEvent, but the score {} cannot be parsed to a double.", scoredSolutionCandidateInfo.getScore());
					return;
				} catch (Exception e) {
					this.logger.error("Could not train classifier! " + e.toString());
				}
			}
		}
	}

	private IClassifier deserializeClassifier(final String serializedClassifier) throws Exception {
		ReconstructionPlan plan = new ObjectMapper().readValue(serializedClassifier, ReconstructionPlan.class);
		return (IClassifier)plan.reconstructObject();
	}

	private double parseScoreToDouble(final String score) throws NumberFormatException {
		return Double.parseDouble(score);
	}

}
