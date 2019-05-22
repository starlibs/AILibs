package de.upb.crc901.mlplan.gui.outofsampleplots;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.core.events.ClassifierFoundEvent;
import jaicore.basic.algorithm.events.serializable.PropertyProcessedAlgorithmEvent;
import jaicore.graphvisualizer.events.gui.GUIEvent;
import jaicore.graphvisualizer.plugin.ASimpleMVCPluginController;
import jaicore.graphvisualizer.plugin.controlbar.ResetEvent;
import jaicore.graphvisualizer.plugin.solutionperformanceplotter.ScoredSolutionCandidateInfo;
import jaicore.graphvisualizer.plugin.solutionperformanceplotter.ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer;
import jaicore.graphvisualizer.plugin.timeslider.GoToTimeStepEvent;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.SerializationHelper;

public class OutOfSampleErrorPlotPluginController extends ASimpleMVCPluginController<OutOfSampleErrorPlotPluginModel, OutOfSampleErrorPlotPluginView> {

	private Instances train, test;
	private Logger logger = LoggerFactory.getLogger(OutOfSampleErrorPlotPlugin.class);

	public OutOfSampleErrorPlotPluginController(OutOfSampleErrorPlotPluginModel model, OutOfSampleErrorPlotPluginView view) {
		super(model, view);
	}

	public Instances getTrain() {
		return train;
	}

	public void setTrain(Instances train) {
		this.train = train;
	}

	public Instances getTest() {
		return test;
	}

	public void setTest(Instances test) {
		this.test = test;
	}

	@Override
	public void handleGUIEvent(GUIEvent guiEvent) {
		if (guiEvent instanceof ResetEvent || guiEvent instanceof GoToTimeStepEvent) {
			getModel().clear();
		}
	}

	@Override
	public void handleAlgorithmEventInternally(PropertyProcessedAlgorithmEvent algorithmEvent) {
		if (algorithmEvent.correspondsToEventOfClass(ClassifierFoundEvent.class)) {
			logger.debug("Received classifier found event {}", algorithmEvent);

			Object rawScoredSolutionCandidateInfo = algorithmEvent.getProperty(ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer.SCORED_SOLUTION_CANDIDATE_INFO_PROPERTY_NAME);
			if (rawScoredSolutionCandidateInfo != null) {
				ScoredSolutionCandidateInfo scoredSolutionCandidateInfo = (ScoredSolutionCandidateInfo) rawScoredSolutionCandidateInfo;

				try {
					Classifier classifier = deserializeClassifier(scoredSolutionCandidateInfo.getSolutionCandidateRepresentation());
					logger.debug("Building classifier");
					classifier.buildClassifier(train);
					Evaluation eval = new Evaluation(train);
					List<Double> performances = new ArrayList<>();
					performances.add(parseScoreToDouble(scoredSolutionCandidateInfo.getScore()));
					eval.evaluateModel(classifier, test);
					performances.add(eval.errorRate());
					logger.debug("Adding solution to model and updating view.");
					getModel().addEntry(algorithmEvent.getTimestampOfEvent(), classifier, performances);
					logger.debug("Added solution to model.");
				} catch (NumberFormatException exception) {
					logger.warn("Received processed SolutionCandidateFoundEvent, but the score {} cannot be parsed to a double.", scoredSolutionCandidateInfo.getScore());
					return;
				} catch (Exception e) {
					logger.error("Could not train classifier! " + e.toString());
				}
			}
		}
	}

	private Classifier deserializeClassifier(String serializedClassifier) throws Exception {
		final byte[] bytes = Base64.getDecoder().decode(serializedClassifier);
		Classifier classifier = (Classifier) SerializationHelper.read(new ByteArrayInputStream(bytes));
		return classifier;
	}

	private double parseScoreToDouble(String score) throws NumberFormatException {
		return Double.parseDouble(score);
	}

}
