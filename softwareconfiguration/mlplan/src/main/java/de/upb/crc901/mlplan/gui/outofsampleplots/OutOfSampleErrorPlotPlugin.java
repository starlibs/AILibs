package de.upb.crc901.mlplan.gui.outofsampleplots;

import jaicore.graphvisualizer.plugin.ASimpleMVCPlugin;
import weka.core.Instances;

public class OutOfSampleErrorPlotPlugin extends ASimpleMVCPlugin<OutOfSampleErrorPlotPluginModel, OutOfSampleErrorPlotPluginView, OutOfSampleErrorPlotPluginController> {

	private final Instances trainData;
	private final Instances testData;
	
	public OutOfSampleErrorPlotPlugin(Instances trainData, Instances testData) {
		super();
		this.trainData = trainData;
		this.testData = testData;
		getController().setTrain(trainData);
		getController().setTest(testData);
	}

	public Instances getTrainData() {
		return trainData;
	}

	public Instances getTestData() {
		return testData;
	}
}
