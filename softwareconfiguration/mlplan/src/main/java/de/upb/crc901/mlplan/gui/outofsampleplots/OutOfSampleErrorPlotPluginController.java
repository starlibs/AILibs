package de.upb.crc901.mlplan.gui.outofsampleplots;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.core.events.ClassifierFoundEvent;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.graphvisualizer.events.gui.GUIEvent;
import jaicore.graphvisualizer.plugin.ASimpleMVCPluginController;
import jaicore.graphvisualizer.plugin.controlbar.ResetEvent;
import jaicore.graphvisualizer.plugin.timeslider.GoToTimeStepEvent;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

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
	public void handleAlgorithmEventInternally(AlgorithmEvent algorithmEvent) {
		if (algorithmEvent instanceof ClassifierFoundEvent) {
			try {
				logger.debug("Received classifier found event {}", algorithmEvent);
				ClassifierFoundEvent event = (ClassifierFoundEvent) algorithmEvent;
				Classifier classifier = (Classifier) event.getSolutionCandidate();
				logger.debug("Building classifier");
				classifier.buildClassifier(train);
				Evaluation eval = new Evaluation(train);
				List<Double> performances = new ArrayList<>();
				performances.add(event.getScore());
				eval.evaluateModel(classifier, test);
				performances.add(eval.errorRate());
				logger.debug("Adding solution to model and updating view.");
				getModel().addEntry(event.getTimestamp(), classifier, performances);
			} catch (Exception e) {
				logger.error("Could not train classifier! " + e.toString());
				e.printStackTrace();
			}
		} else
			logger.trace("Received and ignored irrelevant event {}", algorithmEvent);
	}

}
