package ai.libs.jaicore.ml.scikitwrapper;

import java.io.File;
import java.io.IOException;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.api4.java.ai.ml.core.exception.TrainingException;

import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.ml.core.EScikitLearnProblemType;

public class ScikitLearnFeatureEngineeringWrapper<P extends IPrediction, B extends IPredictionBatch> extends AScikitLearnWrapper<P, B> {

	public ScikitLearnFeatureEngineeringWrapper(final String pipeline, final String imports) throws IOException {
		super(EScikitLearnProblemType.FEATURE_ENGINEERING, pipeline, imports);
	}

	@Override
	protected boolean doLabelFitToProblemType(final ILabeledDataset<? extends ILabeledInstance> data) {
		return true;
	}

	@Override
	public String getDataName(final ILabeledDataset<? extends ILabeledInstance> data) {
		return data.getRelationName();
	}

	@Override
	public File getOutputFile(final String dataName) {
		return new File(this.scikitLearnWrapperConfig.getTempFolder(), this.configurationUID + "_" + dataName + ".arff"); // TODO extension
	}

	@Override
	protected String[] constructCommandLineParametersForFitMode(final File modelFile, final File trainingDataFile, final File outputFile) {
		ScikitLearnWrapperCommandBuilder commandBuilder = this.getCommandBuilder();
		commandBuilder.withFitMode();
		commandBuilder.withModelFile(modelFile);
		commandBuilder.withFitDataFile(trainingDataFile);
		commandBuilder.withFitOutputFile(outputFile);
		return commandBuilder.toCommandArray();
	}

	@Override
	protected String[] constructCommandLineParametersForFitAndPredictMode(final File trainingDataFile, final File trainingOutputFile, final File testingDataFile, final File testingOutputFile) {
		ScikitLearnWrapperCommandBuilder commandBuilder = this.getCommandBuilder();
		commandBuilder.withFitAndPredictMode();
		commandBuilder.withFitDataFile(trainingDataFile);
		commandBuilder.withFitOutputFile(trainingOutputFile);
		commandBuilder.withPredictDataFile(testingDataFile);
		commandBuilder.withPredictOutputFile(testingOutputFile);
		return commandBuilder.toCommandArray();
	}

	@Override
	protected B handleOutput(final File outputFile) throws TrainingException {
		if (!new File(outputFile.getAbsolutePath().replace("test", "train")).exists() || !new File(outputFile.getAbsolutePath()).exists()) {
			FileUtil.touch(outputFile.getAbsolutePath().replace("test", "train"));
			FileUtil.touch(outputFile.getAbsolutePath());
			throw new TrainingException("Executing python failed.");
		}
		return null;
	}

}
