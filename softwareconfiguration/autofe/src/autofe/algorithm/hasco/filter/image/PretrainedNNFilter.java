package autofe.algorithm.hasco.filter.image;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.zoo.PretrainedType;
import org.deeplearning4j.zoo.ZooModel;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.meta.IFilter;
import autofe.util.DataSet;

public class PretrainedNNFilter implements IFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(PretrainedNNFilter.class);

	private ZooModel model;
	private ComputationGraph compGraph;
	
	private int selectedLayer;
	
	
	public PretrainedNNFilter(final ZooModel model, final int selectedLayer) {
		this.model = model;
		this.selectedLayer = selectedLayer;
		try {
			this.compGraph = (ComputationGraph) this.model.initPretrained(PretrainedType.CIFAR10);
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
		for(INDArray example : inputData.getIntermediateInstances()) {
			Map<String, INDArray> result = this.compGraph.feedForward(example, this.selectedLayer, false);
			Object[] values = result.values().toArray();
			INDArray resultMatrix = (INDArray) values[values.length - 1];
			transformedInstances.add(resultMatrix);
			logger.info(resultMatrix.toString());
			
		}
		
		return new DataSet(inputData.getInstances(), transformedInstances);
	}
	
}
