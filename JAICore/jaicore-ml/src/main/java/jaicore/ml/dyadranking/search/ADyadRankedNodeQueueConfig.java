package jaicore.ml.dyadranking.search;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.ml.dyadranking.algorithm.IDyadRanker;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.util.AbstractDyadScaler;
import jaicore.ml.dyadranking.util.DyadMinMaxScaler;
import jaicore.search.algorithms.standard.bestfirst.IBestFirstQueueConfiguration;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

/**
 * A configuration for a dyad ranked node queue. Mainly configures the dyad
 * ranker.
 * 
 * @author Helena Graf
 *
 * @param <N>
 */
public abstract class ADyadRankedNodeQueueConfig<N>
		implements IBestFirstQueueConfiguration<GraphSearchWithSubpathEvaluationsInput<N, String, Double>, N, String, Double> {
	
	private Logger logger = LoggerFactory.getLogger(ADyadRankedNodeQueueConfig.class);

	/**
	 * the ranker used to rank dyads consisting of pipeline metafeatures and dataset
	 * metafeatures
	 */
	protected IDyadRanker ranker;
	
	/** 
	 * for scaling the dyads 
	 */
	protected AbstractDyadScaler scaler;

	/**
	 * Construct a new dyad ranking node queue configuration.
	 * 
	 * @throws IOException if the files for the default ranker and scaler are invalid or cannot be found
	 * @throws ClassNotFoundException  if the default scaler or ranker cannot be instantiated
	 */
	public ADyadRankedNodeQueueConfig() throws IOException, ClassNotFoundException {
		logger.trace("Load MinMaxScaler");	
		FileInputStream fis = new FileInputStream(new File("resources/draco/partial_pipeline_ranking/models/minmax_2500.ser"));
		try(ObjectInputStream ois = new ObjectInputStream(fis)) {
			scaler = (DyadMinMaxScaler) ois.readObject();
			fis.close();
		}
				
		logger.trace("Load PL-Net Dyad Ranker");
		PLNetDyadRanker plranker = new PLNetDyadRanker();
		plranker.loadModelFromFile("resources/draco/partial_pipeline_ranking/models/ranker_2500.zip");
		ranker = plranker;
	}

	/**
	 * Set the ranker used to rank the OPEN list.
	 * 
	 * @return ranker the used ranker
	 */
	public IDyadRanker getRanker() {
		return ranker;
	}

	/**
	 * Set the ranker used to rank the OPEN list.
	 * 
	 * @param ranker the used ranker
	 */
	public void setRanker(IDyadRanker ranker) {
		this.ranker = ranker;
	}

	/**
	 * Get the scaler used to scale the dataset. Is pre-fit.
	 * 
	 * @return the used scaler
	 */
	public AbstractDyadScaler getScaler() {
		return scaler;
	}

	/**
	 * Set the scaler used to scale the dataset. Must be pre-fit.
	 * 
	 * @param scaler the used scaler
	 */
	public void setScaler(AbstractDyadScaler scaler) {
		this.scaler = scaler;
	}
}
