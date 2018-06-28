package autofe.algorithm.hasco.evaluation;

import java.util.Arrays;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.util.DataSet;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.FilteredClusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Nystroem;
import weka.filters.unsupervised.attribute.Remove;

// This is used for the search guidance
public class ClusterEvaluator extends AbstractHASCOFEObjectEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(ClusterEvaluator.class);
	
	@Override
	public Double evaluate(FilterPipeline object) throws Exception {
		
		logger.info("Starting cluster evaluation...");
		no.uib.cipr.matrix.Vector vector;

		DataSet dataSet = object.applyFilter(this.data, false);
		
		// this.data
//		 = (DataSet<T>) this.data;
		Instances insts = dataSet.getInstances();
		
		FilteredClusterer clusterer = new FilteredClusterer();
		
	    Remove filter = new Remove();
	    filter.setAttributeIndices("" + (insts.classIndex() + 1));
	    filter.setInputFormat(insts);
		Instances removedClassInstances = Filter.useFilter(insts, filter);
				
		Nystroem kernelFilter = new Nystroem();
		// TODO: Initialize kernel? (using data, cache size 250007, gamma 0.01)? => Defaults
		kernelFilter.setKernel(new RBFKernel());
		clusterer.setFilter(kernelFilter);
		((SimpleKMeans) clusterer.getClusterer()).setNumClusters(insts.classAttribute().numValues());
		((weka.core.EuclideanDistance) ((SimpleKMeans) clusterer.getClusterer()).getDistanceFunction()).setDontNormalize(true);
		
		clusterer.buildClusterer(removedClassInstances);
		
		ClusterEvaluation clusterEval = new ClusterEvaluation();
		clusterEval.setClusterer(clusterer);
		clusterEval.evaluateClusterer(insts);
		
		logger.info("ClusterEvaluator results: " + clusterEval.clusterResultsToString());
		logger.info("Log likelihood:" + clusterEval.getLogLikelihood());
		logger.info(Arrays.toString(clusterEval.getClassesToClusters()));
		
		// TODO Auto-generated method stub
		return new Random().nextDouble() * 10;
	}

}
