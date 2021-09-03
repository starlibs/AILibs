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

public class ScikitLearnRegressionWrapper<P extends IPrediction, B extends IPredictionBatch> extends AScikitLearnWrapper<P, B> {

	public ScikitLearnRegressionWrapper(final String pipeline, final String imports) throws IOException, InterruptedException {
		super(EScikitLearnProblemType.REGRESSION, pipeline, imports);
	}

	protected ScikitLearnRegressionWrapper(final EScikitLearnProblemType problemType, final String pipeline, final String imports) throws IOException, InterruptedException {
		super(problemType, pipeline, imports);
	}

	@Override
	protected boolean doLabelsFitToProblemType(final ILabeledDataset<? extends ILabeledInstance> data) {
		return data.getLabelAttribute() instanceof NumericAttribute;
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
