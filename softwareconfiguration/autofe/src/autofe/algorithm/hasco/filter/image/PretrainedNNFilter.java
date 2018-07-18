package autofe.algorithm.hasco.filter.image;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
	 * activations when predicting. Is null if neural net can not be applied to
	 * given input shape.
	 */
	private ComputationGraph compGraph = null;

	public static final Map<String, PretrainedType> PRETRAINED_WEIGHTS_MAPPING = new HashMap<>();

	/**
	 * Layer used for feature extraction
	 */
	private int selectedLayer;

	static {
		PRETRAINED_WEIGHTS_MAPPING.put("AlexNet", PretrainedType.IMAGENET);
		PRETRAINED_WEIGHTS_MAPPING.put("LeNet", PretrainedType.MNIST);
		PRETRAINED_WEIGHTS_MAPPING.put("ResNet50", PretrainedType.IMAGENET);
		PRETRAINED_WEIGHTS_MAPPING.put("VGG16", PretrainedType.IMAGENET);
		PRETRAINED_WEIGHTS_MAPPING.put("VGG19", PretrainedType.IMAGENET);
	}

	public PretrainedNNFilter(final ZooModel model, final int selectedLayer, final int[] shape) {
		if (shape[0] != 1)
			throw new IllegalArgumentException(
					"Given input shape assumes one example as batch input (first input dimension has to be 1).");
		if (shape.length < 3)
			throw new IllegalArgumentException(
					"Given input shape assumes to have spatial structure (at least two dimensions plus batch size dimension).");

		this.model = model;
		this.selectedLayer = selectedLayer;

		try {
			PretrainedType type = inferPretrainedTypeFromShape(shape);
			if (this.model.pretrainedAvailable(type))
				this.compGraph = ((MultiLayerNetwork) this.model.initPretrained(PretrainedType.MNIST))
						.toComputationGraph();
			else
				logger.warn("Given model " + this.model.getClass().getSimpleName()
						+ " does not provide pretrained weights for input shape " + Arrays.toString(shape)
						+ " (pretrained type " + type + ").");
		} catch (UnsupportedOperationException ex) {
			logger.warn(ex.getMessage());
			logger.warn(model.getClass().getName());
		} catch (IOException e) {
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

			// Add channel dimension
			if (shape.length < 3) {
				example = example.reshape(shape[0], shape[1], 1);
			}

			// Transforms example to (batch size, channel, width, height) shape
			INDArray adjustedExample = example.permute(2, 0, 1);
			adjustedExample = adjustedExample.reshape(1, adjustedExample.shape()[0], adjustedExample.shape()[1],
					adjustedExample.shape()[2]);

			// Get activations of neural net per layer and store them
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

	// // Assumes input shape (batch size, channels, width, height, [depth])
	// private static PretrainedType inferPretrainedTypeFromShape(final int[] shape)
	// {
	//
	// return PretrainedType.CIFAR10;
	// }

	// Assumes input shape (batch size, channels, width, height, [depth])
	private static PretrainedType inferPretrainedTypeFromShape(final int[] shape) {
		// Grayscale structure
		if (shape.length == 4 && shape[1] == 1)
			return PretrainedType.MNIST;
		if (shape.length == 4 && shape[2] > 32) {
			if (shape[2] > 32)
				return PretrainedType.IMAGENET;
		}
		return PretrainedType.CIFAR10;
	}

}
