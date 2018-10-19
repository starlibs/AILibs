package autofe.algorithm.hasco.filter.image;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.zoo.PretrainedType;
import org.deeplearning4j.zoo.ZooModel;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.meta.IFilter;
import autofe.util.DataSet;
import autofe.util.ImageUtils;

/**
 * Wrapper filter for pretrained neural nets taken from Deeplearning4j framework
 * (e. g. AlexNet, VGG16, ...).
 *
 * @author Julian Lienen
 *
 */
@SuppressWarnings("rawtypes")
public class PretrainedNNFilter implements IFilter, Serializable {

	/**
	 * Default generated serial version UID
	 */
	private static final long serialVersionUID = 3181364718254679581L;

	private static final Logger logger = LoggerFactory.getLogger(PretrainedNNFilter.class);

	/**
	 * Neural net model (description of the net)
	 */
	private ZooModel model;

	/**
	 * Name of the neural net model
	 */
	private String modelName;

	/**
	 * Computational graph (concrete layered structure of the net). Used to access
	 * activations when predicting. Is null if neural net can not be applied to
	 * given input shape.
	 */
	private ComputationGraph compGraph = null;

	/**
	 * Layer used for feature extraction (currently not used due to compatibility
	 * issues)
	 */
	private int selectedLayer;

	/**
	 * Flag if grayscale to RGB conversion is necessary (e. g. for usage of
	 * pretrained nets using ImageNet for grayscale data)
	 */
	private boolean convertGrayscaleToRGB = false;

	/**
	 * Flag if RGB to grayscale conversion is necessary (e. g. for usage of LeNet on
	 * RGB data)
	 */
	private boolean convertRGBToGrayscale = false;

	public static final Map<String, List<PretrainedType>> PRETRAINED_WEIGHTS_MAPPING = new HashMap<>();
	static {
		PRETRAINED_WEIGHTS_MAPPING.put("AlexNet", Arrays.asList(PretrainedType.IMAGENET));
		PRETRAINED_WEIGHTS_MAPPING.put("LeNet", Arrays.asList(PretrainedType.MNIST));
		PRETRAINED_WEIGHTS_MAPPING.put("ResNet50", Arrays.asList(PretrainedType.IMAGENET));
		PRETRAINED_WEIGHTS_MAPPING.put("VGG16",
				Arrays.asList(PretrainedType.IMAGENET, PretrainedType.CIFAR10, PretrainedType.VGGFACE));
		PRETRAINED_WEIGHTS_MAPPING.put("VGG19", Arrays.asList(PretrainedType.IMAGENET));
	}

	public PretrainedNNFilter(final ZooModel model, final int selectedLayer, final int[] shape,
			final String modelName) {
		// if (shape.length > 3 && shape[0] != 1) {
		// throw new IllegalArgumentException(
		// "Given input shape assumes one example as batch input (first input dimension
		// has to be 1).");
		// }
		if (shape.length < 2) {
			throw new IllegalArgumentException(
					"Given input shape assumes to have spatial structure (at least two dimensions).");
		}

		this.model = model;
		this.modelName = modelName;
		this.selectedLayer = selectedLayer;

		try {
			PretrainedType type = inferPretrainedTypeFromShape(shape);

			if (!this.model.pretrainedAvailable(type)) {
				logger.warn("Given model " + this.model.getClass().getSimpleName()
						+ " does not provide pretrained weights for input shape " + Arrays.toString(shape)
						+ " (pretrained type " + type + "). Trying to find different weights and adjust input shape.");

				List<PretrainedType> availableWeights = PRETRAINED_WEIGHTS_MAPPING.get(modelName);
				if (availableWeights == null) {
					String errorMessage = "Could not infer available weights for model '" + modelName + "'.";
					logger.error(errorMessage);
					throw new IllegalStateException(errorMessage);
				}
				if (shape[2] == 1) {
					// Force RGB
					this.convertGrayscaleToRGB = true;
					if (shape[0] < 32 && shape[1] < 32 && availableWeights.contains(PretrainedType.CIFAR10)) {
						type = PretrainedType.CIFAR10;
					} else if (availableWeights.contains(PretrainedType.IMAGENET)) {
						type = PretrainedType.IMAGENET;
					} else {
						type = null;
					}
				} else {
					// Special case for LeNet (can only deal with one input channel)

					if (shape[2] > 1 && modelName.equals("LeNet")) {
						// Force to grayscale conversion for RGB data on LeNet pretrained weights
						this.convertRGBToGrayscale = true;
						type = PretrainedType.MNIST;
					} else {
						type = availableWeights.get(0);
					}
				}
			}

			if (type == null) {
				logger.warn("Could not infer any type for model '" + modelName + "' and shape '"
						+ Arrays.toString(shape) + "'.");
				return;
			}

			logger.debug("Using pretrained weights " + type);

			Model pretrainedModel = this.model.initPretrained(type);
			if (pretrainedModel instanceof ComputationGraph) {
				this.compGraph = (ComputationGraph) this.model.initPretrained(type);
			} else {
				this.compGraph = ((MultiLayerNetwork) this.model.initPretrained(type)).toComputationGraph();
			}

		} catch (UnsupportedOperationException ex) {
			logger.warn(ex.getMessage());
			logger.warn(model.getClass().getName());
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public DataSet applyFilter(final DataSet inputData, final boolean copy) {
		if (inputData.getIntermediateInstances() == null || inputData.getIntermediateInstances().size() == 0
				|| inputData.getIntermediateInstances().get(0).rank() < 2) {
			throw new IllegalArgumentException(
					"Intermediate instances must have a rank of at least 2 for image processing.");
		}

		List<INDArray> transformedInstances = new ArrayList<>(inputData.getIntermediateInstances().size());

		if (this.convertGrayscaleToRGB) {
			logger.debug("Instances have to be converted from grayscale to RGB...");
			inputData.setIntermediateInstances(ImageUtils.grayscaleMatricesToRGB(inputData.getIntermediateInstances()));
			logger.debug("Done conversion from grayscale to RGB.");
		} else if (this.convertRGBToGrayscale) {
			logger.debug("Instances have to be converted from RGB to grayscale...");
			inputData.setIntermediateInstances(ImageUtils.rgbMatricesToGrayscale(inputData.getIntermediateInstances()));
			logger.debug("Done conversion from RGB to grayscale.");
		}

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

			Map<String, INDArray> result = this.compGraph.feedForward(adjustedExample,
					this.getSelectedLayerByModelName(), false); // this.selectedLayer

			// logger.debug("Feed forward end " + this.model.getClass().getName());
			Object[] values = result.values().toArray();
			INDArray resultMatrix = (INDArray) values[values.length - 1];
			transformedInstances.add(resultMatrix);
		}

		return new DataSet(inputData.getInstances(), transformedInstances);
	}

	public ZooModel getModel() {
		return this.model;
	}

	public ComputationGraph getCompGraph() {
		return this.compGraph;
	}

	public int getSelectedLayer() {
		return this.selectedLayer;
	}

	public int getSelectedLayerByModelName() {
		if (this.modelName.equalsIgnoreCase("VGG16")) {
			return this.compGraph.getNumLayers() - 5;
		} else {
			return this.compGraph.getNumLayers() - 3;
		}
	}

	// Assumes input shape (width, height, channels, [depth])
	private static PretrainedType inferPretrainedTypeFromShape(final int[] shape) {

		if (shape.length <= 2 || (shape.length == 3 && shape[2] == 1)) {
			// Grayscale
			return PretrainedType.MNIST;
		} else if (shape[0] > 32 || shape[1] > 32) {
			// Large RGB images
			return PretrainedType.IMAGENET;
		} else {
			// Small RGB images
			return PretrainedType.CIFAR10;
		}
	}

	@Override
	public String toString() {
		if (this.model != null) {
			return "PretrainedNNFilter [model=" + this.model.getClass().getSimpleName() + ", selectedLayer="
					+ this.selectedLayer + "]";
		} else {
			return "PretrainedNNFilter [model=" + this.model + ", selectedLayer=" + this.selectedLayer + "]";
		}
	}

}
