package ai.libs.mlplan.gui.outofsampleplots;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.algorithm.events.AlgorithmEvent;
import ai.libs.jaicore.graphvisualizer.events.gui.GUIEvent;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginController;
import ai.libs.jaicore.graphvisualizer.plugin.controlbar.ResetEvent;
import ai.libs.jaicore.graphvisualizer.plugin.timeslider.GoToTimeStepEvent;
import ai.libs.mlplan.core.events.ClassifierFoundEvent;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class OutOfSampleErrorPlotPluginController extends ASimpleMVCPluginController<OutOfSampleErrorPlotPluginModel, OutOfSampleErrorPlotPluginView> {

	private Instances train;
	private Instances test;
	private Logger logger = LoggerFactory.getLogger(OutOfSampleErrorPlotPluginController.class);

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
	public void handleAlgorithmEventInternally(final AlgorithmEvent algorithmEvent) {
		if (algorithmEvent instanceof ClassifierFoundEvent) {
			try {
				this.logger.debug("Received classifier found event {}", algorithmEvent);
				ClassifierFoundEvent event = (ClassifierFoundEvent) algorithmEvent;
				Classifier classifier = event.getSolutionCandidate();
				this.logger.debug("Building classifier");
				classifier.buildClassifier(this.train);
				Evaluation eval = new Evaluation(this.train);
				List<Double> performances = new ArrayList<>();
				performances.add(event.getScore());
				eval.evaluateModel(classifier, this.test);
				performances.add(eval.errorRate());
				this.logger.debug("Adding solution to model and updating view.");
				this.getModel().addEntry(event.getTimestamp(), classifier, performances);
			} catch (Exception e) {
				this.logger.error("Could not train classifier: {}", e);
			}
		} else {
			this.logger.trace("Received and ignored irrelevant event {}", algorithmEvent);
		}
	}

}
