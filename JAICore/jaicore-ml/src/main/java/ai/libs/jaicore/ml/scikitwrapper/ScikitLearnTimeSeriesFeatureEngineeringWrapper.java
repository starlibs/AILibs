package ai.libs.jaicore.ml.scikitwrapper;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;

import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.ml.core.EScikitLearnProblemType;

public class ScikitLearnTimeSeriesFeatureEngineeringWrapper<P extends IPrediction, B extends IPredictionBatch> extends AScikitLearnWrapper<P, B> {

	public ScikitLearnTimeSeriesFeatureEngineeringWrapper(final String pipeline, final String imports) throws IOException, InterruptedException {
		super(EScikitLearnProblemType.TIME_SERIES_FEATURE_ENGINEERING, pipeline, imports);
	}

	@Override
	protected boolean doLabelsFitToProblemType(final ILabeledDataset<? extends ILabeledInstance> data) {
		return true;
	}

	@Override
	public String getDataName(final ILabeledDataset<? extends ILabeledInstance> data) {
		return data.getRelationName();
	}

	@Override
	public File getOutputFile(final String dataName) {
		return new File(this.scikitLearnWrapperConfig.getTempFolder(), this.configurationUID + "_" + dataName + ".arff");
	}

	@Override
	protected ScikitLearnWrapperCommandBuilder getCommandBuilder() {
		ScikitLearnTimeSeriesFeatureEngineeringWrapperCommandBuilder commandBuilder = new ScikitLearnTimeSeriesFeatureEngineeringWrapperCommandBuilder(this.problemType.getScikitLearnCommandLineFlag(), this.getSKLearnScriptFile());
		return super.getCommandBuilder(commandBuilder);
	}

	@Override
	protected ScikitLearnWrapperCommandBuilder constructCommandLineParametersForFitMode(final File modelFile, final File trainingDataFile, final File outputFile) {
		ScikitLearnWrapperCommandBuilder commandBuilder = this.getCommandBuilder();
		commandBuilder.withFitMode();
		commandBuilder.withModelFile(modelFile);
		commandBuilder.withFitDataFile(trainingDataFile);
		commandBuilder.withFitOutputFile(outputFile);
		return commandBuilder;
	}

	@Override
	protected ScikitLearnWrapperCommandBuilder constructCommandLineParametersForFitAndPredictMode(final File trainingDataFile, final File trainingOutputFile, final File testingDataFile, final File testingOutputFile) {
		ScikitLearnWrapperCommandBuilder commandBuilder = this.getCommandBuilder();
		commandBuilder.withFitAndPredictMode();
		commandBuilder.withFitDataFile(trainingDataFile);
		commandBuilder.withFitOutputFile(trainingOutputFile);
		commandBuilder.withPredictDataFile(testingDataFile);
		commandBuilder.withPredictOutputFile(testingOutputFile);
		return commandBuilder;
	}

	@Override
	protected B handleOutput(final File outputFile) throws TrainingException {
		if (!outputFile.exists()) {
			FileUtil.touch(outputFile.getAbsolutePath());
			throw new TrainingException("Executing python failed.");
		}
		return null;
	}

	@Override
	protected B handleOutput(final File fitOutputFile, final File predictOutputFile) throws PredictionException, TrainingException {
		this.handleOutput(fitOutputFile);
		this.handleOutput(predictOutputFile);
		return null;
	}

	class ScikitLearnTimeSeriesFeatureEngineeringWrapperCommandBuilder extends ScikitLearnWrapperCommandBuilder {

		protected ScikitLearnTimeSeriesFeatureEngineeringWrapperCommandBuilder(final String problemTypeFlag, final File scriptFile) {
			super(problemTypeFlag, scriptFile);
		}

		@Override
		protected void checkRequirementsTrainMode() {
			Objects.requireNonNull(this.fitDataFile);
			Objects.requireNonNull(this.modelFile);
			Objects.requireNonNull(this.fitOutputFile);
		}

		@Override
		protected void checkRequirementsTrainTestMode() {
			Objects.requireNonNull(this.fitDataFile);
			Objects.requireNonNull(this.fitOutputFile);
			Objects.requireNonNull(this.predictDataFile);
			Objects.requireNonNull(this.predictOutputFile);
		}

	}

}
