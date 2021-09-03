package ai.libs.jaicore.ml.scikitwrapper.simple;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.regression.evaluation.IRegressionPrediction;
import org.api4.java.ai.ml.regression.evaluation.IRegressionResultBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import ai.libs.jaicore.ml.regression.singlelabel.SingleTargetRegressionPrediction;
import ai.libs.jaicore.ml.regression.singlelabel.SingleTargetRegressionPredictionBatch;
import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnWrapperExecutionFailedException;

public class SimpleScikitLearnRegressor extends ASimpleScikitLearnWrapper<IRegressionPrediction, IRegressionResultBatch> {
	private Logger logger = LoggerFactory.getLogger(SimpleScikitLearnRegressor.class);

	public SimpleScikitLearnRegressor(final String constructorCall, final String imports) throws IOException, InterruptedException {
		super(constructorCall, imports, "regression");
	}

	@Override
	public IRegressionResultBatch predict(final ILabeledDataset<? extends ILabeledInstance> dTest) throws PredictionException, InterruptedException {
		IRegressionResultBatch batch = null;
		try {
			File predictOutputFile = this.executePipeline(dTest);
			JsonNode n = new ObjectMapper().readTree(FileUtils.readFileToString(predictOutputFile));
			if (!(n instanceof ArrayNode)) {
				throw new PredictionException("Json file for predictions does not contain an array as root element");
			}

			List<IRegressionPrediction> predictions = new ArrayList<>();
			ArrayNode preds = (ArrayNode) n;
			for (JsonNode pred : preds) {
				predictions.add(new SingleTargetRegressionPrediction(pred.asDouble()));
			}
			batch = new SingleTargetRegressionPredictionBatch(predictions);
		} catch (InterruptedException e) {
			this.logger.info("SimpleScikitLearnRegressor for pipeline {} got interrupted.", this.constructorCall);
			throw e;
		} catch (IOException e) {
			throw new PredictionException("Could not write executable python file.", e);
		} catch (ScikitLearnWrapperExecutionFailedException e) {
			throw new PredictionException("Could not execute scikit learn wrapper", e);
		}
		return batch;
	}

}
