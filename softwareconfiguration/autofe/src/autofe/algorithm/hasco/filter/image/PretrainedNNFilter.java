package autofe.algorithm.hasco.filter.image;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.zoo.PretrainedType;
import org.deeplearning4j.zoo.ZooModel;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.meta.IFilter;
import autofe.util.DataSet;

/**
 * Wrapper filter for pretrained neural nets taken from Deeplearning4j framework
 * (e. g. AlexNet, VGG16, ...).
 * 
 * @author Julian Lienen
 *
 */
@SuppressWarnings("rawtypes")
public class PretrainedNNFilter implements IFilter {

	private static final Logger logger = LoggerFactory.getLogger(PretrainedNNFilter.class);

	/**
	 * Neural net model (description of the net)
	 */
	private ZooModel model;

	/**
	 * Computational graph (concrete layered structure of the net). Used to access
	 * activations when predicting.
	 */
	private ComputationGraph compGraph;

	/**
	 * Layer used for feature extraction
	 */
	private int selectedLayer;

	public PretrainedNNFilter(final ZooModel model, final int selectedLayer) {
		this.model = model;
		this.selectedLayer = selectedLayer;
		try {
			this.compGraph = ((MultiLayerNetwork) this.model.initPretrained(PretrainedType.MNIST)).toComputationGraph();
		} catch (UnsupportedOperationException ex) {
			logger.error(ex.getMessage());
			ex.printStackTrace();
			logger.error(model.getClass().getName());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public DataSet applyFilter(DataSet inputData, boolean copy) {

		if (inputData.getIntermediateInstances() == null || inputData.getIntermediateInstances().size() == 0
				|| inputData.getIntermediateInstances().get(0).rank() < 2)
			throw new IllegalArgumentException(
					"Intermediate instances must have a rank of at least 2 for image processing.");

		List<INDArray> transformedInstances = new ArrayList<>(inputData.getIntermediateInstances().size());
		for (INDArray example : inputData.getIntermediateInstances()) {
			int[] shape = example.shape();
			if (shape.length < 3) {
				example = example.reshape(shape[0], shape[1], 1);
			}

			// TODO: Generic approach
			INDArray adjustedExample = example.permute(2, 0, 1);
			adjustedExample = adjustedExample.reshape(1, adjustedExample.shape()[0], adjustedExample.shape()[1],
					adjustedExample.shape()[2]);
			Map<String, INDArray> result = this.compGraph.feedForward(adjustedExample, this.selectedLayer, false);
			Object[] values = result.values().toArray();
			INDArray resultMatrix = (INDArray) values[values.length - 1];
			transformedInstances.add(resultMatrix);
		}

		return new DataSet(inputData.getInstances(), transformedInstances);
	}

	public ZooModel getModel() {
		return model;
	}

	public ComputationGraph getCompGraph() {
		return compGraph;
	}

	public int getSelectedLayer() {
		return selectedLayer;
	}

}
