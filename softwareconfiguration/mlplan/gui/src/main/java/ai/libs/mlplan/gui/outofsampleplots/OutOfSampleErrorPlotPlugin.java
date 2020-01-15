package ai.libs.mlplan.gui.outofsampleplots;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;

import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPlugin;

public class OutOfSampleErrorPlotPlugin extends ASimpleMVCPlugin<OutOfSampleErrorPlotPluginModel, OutOfSampleErrorPlotPluginView, OutOfSampleErrorPlotPluginController> {

	private final ILabeledDataset<?> trainData;
	private final ILabeledDataset<?> testData;

	public OutOfSampleErrorPlotPlugin(final ILabeledDataset<?> trainData, final ILabeledDataset<?> testData) {
		super();
		this.trainData = trainData;
		this.testData = testData;
		this.getController().setTrain(trainData);
		this.getController().setTest(testData);
	}

	public ILabeledDataset<?> getTrainData() {
		return this.trainData;
	}

	public ILabeledDataset<?> getTestData() {
		return this.testData;
	}
}
