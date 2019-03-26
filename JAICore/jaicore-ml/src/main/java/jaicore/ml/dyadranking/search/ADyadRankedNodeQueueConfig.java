package jaicore.ml.dyadranking.search;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.ml.dyadranking.algorithm.ADyadRanker;
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
	protected ADyadRanker ranker;
	
	/** for scaling the dyads */
	protected AbstractDyadScaler scaler;

	/**
	 * Construct a new dyad ranking node queue configuration.
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public ADyadRankedNodeQueueConfig() throws IOException, ClassNotFoundException {
		logger.trace("Load MinMaxScaler");	
		FileInputStream fis = new FileInputStream(new File("conf/dyadranking/minmaxscaler.ser"));
		ObjectInputStream ois = new ObjectInputStream(fis);		
		scaler = (DyadMinMaxScaler) ois.readObject();
		fis.close();
		ois.close();
		
		logger.trace("Load PL-Net Dyad Ranker");
		PLNetDyadRanker plranker = new PLNetDyadRanker();
		plranker.loadModelFromFile("conf/dyadranking/final_plnet_minmax.zip");
		ranker = plranker;
	}

	public ADyadRanker getRanker() {
		return ranker;
	}

	public void setRanker(ADyadRanker ranker) {
		this.ranker = ranker;
	}

	public AbstractDyadScaler getScaler() {
		return scaler;
	}

	public void setScaler(AbstractDyadScaler scaler) {
		this.scaler = scaler;
	}
}
