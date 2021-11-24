package ai.libs.jaicore.ml.scikitwrapper.simple;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;
import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassificationPredictionBatch;
import org.api4.java.ai.ml.classification.singlelabel.learner.ISingleLabelClassifier;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.PredictionException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;

import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassification;
import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassificationPredictionBatch;
import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnWrapperExecutionFailedException;

public class SimpleScikitLearnClassifier extends ASimpleScikitLearnWrapper<ISingleLabelClassification, ISingleLabelClassificationPredictionBatch> implements ISingleLabelClassifier {

	public SimpleScikitLearnClassifier(final String constructorCall, final String imports) throws IOException, InterruptedException {
		super(constructorCall, imports, "classification");
	}

	@Override
	public ISingleLabelClassificationPredictionBatch predict(final ILabeledDataset<? extends ILabeledInstance> dTest) throws PredictionException, InterruptedException {
		ISingleLabelClassificationPredictionBatch batch = null;
		try {
			File predictOutputFile = this.executePipeline(dTest);
			List<String> labels = ((ICategoricalAttribute) dTest.getLabelAttribute()).getLabels();
			JsonNode n = new ObjectMapper().readTree(FileUtil.readFileAsString(predictOutputFile));
			if (!(n instanceof ArrayNode)) {
				throw new PredictionException("Json file for predictions does not contain an array as root element");
			}

			List<String> ascendSortingLabels = new ArrayList<>(labels);
			Collections.sort(ascendSortingLabels);

			List<ISingleLabelClassification> predictions = new ArrayList<>();
			ArrayNode preds = (ArrayNode) n;
			for (JsonNode pred : preds) {
				double[] labelProbabilities = new double[labels.size()];
				if (pred instanceof ArrayNode) {
					int i = 0;
					for (JsonNode prob : pred) {
						labelProbabilities[labels.indexOf(ascendSortingLabels.get(i++))] = prob.asDouble();
					}
				} else if (pred instanceof TextNode) {
					int index = (int) dTest.getLabelAttribute().deserializeAttributeValue(pred.asText());
					labelProbabilities[index] = 1.0;
				}
				predictions.add(new SingleLabelClassification(labelProbabilities));
			}
			batch = new SingleLabelClassificationPredictionBatch(predictions);
		} catch (InterruptedException e) {
			throw e;
		} catch (IOException e) {
			throw new PredictionException("Could not write executable python file.", e);
		} catch (ScikitLearnWrapperExecutionFailedException e) {
			throw new PredictionException("Could not execute scikit learn wrapper", e);
		}
		return batch;
	}

}
