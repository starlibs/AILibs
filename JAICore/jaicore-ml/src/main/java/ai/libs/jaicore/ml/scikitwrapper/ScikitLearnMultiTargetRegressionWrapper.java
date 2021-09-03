package ai.libs.jaicore.ml.scikitwrapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;

import ai.libs.jaicore.ml.core.EScikitLearnProblemType;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.NumericAttribute;
import ai.libs.jaicore.ml.regression.singlelabel.SingleTargetRegressionPrediction;
import ai.libs.jaicore.ml.regression.singlelabel.SingleTargetRegressionPredictionBatch;

public class ScikitLearnMultiTargetRegressionWrapper<P extends IPrediction, B extends IPredictionBatch> extends AScikitLearnWrapper<P, B> {

	public ScikitLearnMultiTargetRegressionWrapper(final String pipeline, final String imports, final int[] targetIndices) throws IOException, InterruptedException {
		super(EScikitLearnProblemType.REGRESSION, pipeline, imports);
		this.targetIndices = targetIndices;
	}

	protected ScikitLearnMultiTargetRegressionWrapper(final EScikitLearnProblemType problemType, final String pipeline, final String imports) throws IOException, InterruptedException {
		super(problemType, pipeline, imports);
	}

	@Override
	protected boolean doLabelsFitToProblemType(final ILabeledDataset<? extends ILabeledInstance> data) {
		for (int i = 0; i < this.targetIndices.length - 1; i++) {
			if (!(data.getAttribute(this.targetIndices[i]) instanceof NumericAttribute)) {
				return false;
			}
		}
		return data.getLabelAttribute() instanceof NumericAttribute;
	}

	@Override
	protected ScikitLearnWrapperCommandBuilder constructCommandLineParametersForFitMode(final File modelFile, final File trainingDataFile, final File outputFile) {
		ScikitLearnWrapperCommandBuilder commandLineBuilder = super.constructCommandLineParametersForFitMode(modelFile, trainingDataFile, outputFile);
		commandLineBuilder.withTargetIndices(this.targetIndices);
		return commandLineBuilder;
	}

	@Override
	protected ScikitLearnWrapperCommandBuilder constructCommandLineParametersForPredictMode(final File modelFile, final File testingDataFile, final File outputFile) {
		ScikitLearnWrapperCommandBuilder commandLineBuilder = super.constructCommandLineParametersForPredictMode(modelFile, testingDataFile, outputFile);
		commandLineBuilder.withTargetIndices(this.targetIndices);
		return commandLineBuilder;
	}

	@Override
	protected ScikitLearnWrapperCommandBuilder constructCommandLineParametersForFitAndPredictMode(final File trainingDataFile, final File trainingOutputFile, final File testingDataFile, final File testingOutputFile) {
		ScikitLearnWrapperCommandBuilder commandLineBuilder = super.constructCommandLineParametersForFitAndPredictMode(trainingDataFile, trainingOutputFile, testingDataFile, testingOutputFile);
		commandLineBuilder.withTargetIndices(this.targetIndices);
		return commandLineBuilder;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected B handleOutput(final File outputFile) throws PredictionException, TrainingException {
		List<List<Double>> rawLastPredictionResults = this.getRawPredictionResults(outputFile);
		if (!rawLastPredictionResults.isEmpty()) {
			return (B) new SingleTargetRegressionPredictionBatch(rawLastPredictionResults.stream().flatMap(List::stream).map(x -> new SingleTargetRegressionPrediction((double) x)).collect(Collectors.toList()));
		}
		throw new PredictionException("Reading the output file lead to empty predictions.");
	}

}
