package ai.libs.mlplan.gui.outofsampleplots;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.api4.java.algorithm.events.serializable.PropertyProcessedAlgorithmEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.graphvisualizer.events.gui.GUIEvent;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginController;
import ai.libs.jaicore.graphvisualizer.plugin.controlbar.ResetEvent;
import ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter.ScoredSolutionCandidateInfo;
import ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter.ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.timeslider.GoToTimeStepEvent;
import ai.libs.mlplan.core.events.ClassifierFoundEvent;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.SerializationHelper;

public class OutOfSampleErrorPlotPluginController extends ASimpleMVCPluginController<OutOfSampleErrorPlotPluginModel, OutOfSampleErrorPlotPluginView> {

	private Instances train, test;
	private Logger logger = LoggerFactory.getLogger(OutOfSampleErrorPlotPlugin.class);

	public OutOfSampleErrorPlotPluginController(final OutOfSampleErrorPlotPluginModel model, final OutOfSampleErrorPlotPluginView view) {
		super(model, view);
	}

	public Instances getTrain() {
		return this.train;
	}

	public void setTrain(final Instances train) {
		this.train = train;
	}

	public Instances getTest() {
		return this.test;
	}

	public void setTest(final Instances test) {
		this.test = test;
	}

	@Override
	public void handleGUIEvent(final GUIEvent guiEvent) {
		if (guiEvent instanceof ResetEvent || guiEvent instanceof GoToTimeStepEvent) {
			this.getModel().clear();
		}
	}

	@Override
	public void handleAlgorithmEventInternally(final PropertyProcessedAlgorithmEvent algorithmEvent) {
		if (algorithmEvent.correspondsToEventOfClass(ClassifierFoundEvent.class)) {
			this.logger.debug("Received classifier found event {}", algorithmEvent);

			Object rawScoredSolutionCandidateInfo = algorithmEvent.getProperty(ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer.SCORED_SOLUTION_CANDIDATE_INFO_PROPERTY_NAME);
			if (rawScoredSolutionCandidateInfo != null) {
				ScoredSolutionCandidateInfo scoredSolutionCandidateInfo = (ScoredSolutionCandidateInfo) rawScoredSolutionCandidateInfo;

				try {
					Classifier classifier = this.deserializeClassifier(scoredSolutionCandidateInfo.getSolutionCandidateRepresentation());
					this.logger.debug("Building classifier");
					classifier.buildClassifier(this.train);
					Evaluation eval = new Evaluation(this.train);
					List<Double> performances = new ArrayList<>();
					performances.add(this.parseScoreToDouble(scoredSolutionCandidateInfo.getScore()));
					eval.evaluateModel(classifier, this.test);
					performances.add(eval.errorRate());
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

	private Classifier deserializeClassifier(final String serializedClassifier) throws Exception {
		final byte[] bytes = Base64.getDecoder().decode(serializedClassifier);
		Classifier classifier = (Classifier) SerializationHelper.read(new ByteArrayInputStream(bytes));
		return classifier;
	}

	private double parseScoreToDouble(final String score) throws NumberFormatException {
		return Double.parseDouble(score);
	}

}
