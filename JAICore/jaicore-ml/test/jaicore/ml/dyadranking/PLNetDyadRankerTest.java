package jaicore.ml.dyadranking;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.aeonbits.owner.ConfigFactory;
import org.junit.Test;

import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.dyadranking.algorithm.IPLNetDyadRankerConfiguration;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.general.DyadRankingInstanceSupplier;

/**
 * Class for testing the functionality of the {@link PLNetDyadRanker}.
 * @author Jonas Hanselle
 *
 */
public class PLNetDyadRankerTest {

	@Test
	public void testPLNetCreation() {
		IPLNetDyadRankerConfiguration config = ConfigFactory.create(IPLNetDyadRankerConfiguration.class);
		config.setProperty(IPLNetDyadRankerConfiguration.K_PLNET_LEARNINGRATE, "0.2");
		config.setProperty(IPLNetDyadRankerConfiguration.K_PLNET_HIDDEN_NODES, "6,4,3");
		config.setProperty(IPLNetDyadRankerConfiguration.K_PLNET_SEED, "2");
		config.setProperty(IPLNetDyadRankerConfiguration.K_ACTIVATION_FUNCTION, "Activation.RELU");
		config.setProperty(IPLNetDyadRankerConfiguration.K_MAX_EPOCHS, "10");
		config.setProperty(IPLNetDyadRankerConfiguration.K_EARLY_STOPPING_INTERVAL, "2");
		config.setProperty(IPLNetDyadRankerConfiguration.K_EARLY_STOPPING_PATIENCE, "5");
		PLNetDyadRanker pldr = new PLNetDyadRanker();
		pldr.setConfiguration(config);

		DyadRankingDataset trainingDataset = DyadRankingInstanceSupplier.getDyadRankingDataset(10, 1000);
		DyadRankingDataset testDataset = DyadRankingInstanceSupplier.getDyadRankingDataset(10, 200);
		
		try {
			pldr.train(trainingDataset);
			List<IDyadRankingInstance> predictions = pldr.predict(testDataset);
			
		} catch (TrainingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PredictionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
