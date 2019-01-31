package de.upb.crc901.automl.metamining.similaritymeasures;

import de.upb.crc901.mlplan.metamining.similaritymeasures.RelativeRankMatricComputer;

public class RankMatrixSimilarityComputerTest {
	public void testComputeSimilarity() {
		double[][][] performanceValues = {{{0.9,0.8,0.9,0.8},null,{0.5,0.5,0.5,0.5}},
										  {{0.9,0.9,0.9,0.9},{0.7,0.6,0.7,0.6},{0.5,0.5,0.5,0.5}}};
		
		System.out.println(new RelativeRankMatricComputer().computeRelativeRankMatrix(performanceValues));
	}
	
	public static void main (String [] args) {
		new RankMatrixSimilarityComputerTest().testComputeSimilarity();
	}
}
