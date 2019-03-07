package jaicore.ml.dyadranking.loss;

import org.junit.Test;

import jaicore.ml.dyadranking.dataset.DyadRankingInstance;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.general.DyadRankingInstanceSupplier;

public class NDCGLossTest {

	@Test
	public void test() {
		DyadRankingInstance drTrue = DyadRankingInstanceSupplier.getDyadRankingInstance(9, 9);
		
	}

}
