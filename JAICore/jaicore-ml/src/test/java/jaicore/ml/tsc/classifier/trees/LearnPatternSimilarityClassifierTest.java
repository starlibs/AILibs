package jaicore.ml.tsc.classifier.trees;

import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

/**
 * Unit tests for {@link LearnPatternSimilarityClassifier}.
 * 
 * @author Julian Lienen
 *
 */
public class LearnPatternSimilarityClassifierTest {

	/**
	 * Classifier instance used within the tests.
	 */
	private LearnPatternSimilarityClassifier classifier;

	/**
	 * Number of segments used within the tests.
	 */
	private static final int NUM_SEGMENTS = 1;

	/**
	 * Number of trees used within the tests.
	 */
	private static final int NUM_TREES = 2;

	/**
	 * Maximum tree depth used within the tests.
	 */
	private static final int MAX_TREE_DEPTH = 10;

	/**
	 * Initialization called before all tests.
	 */
	@Before
	public void setup() {
		classifier = new LearnPatternSimilarityClassifier(42, NUM_TREES, MAX_TREE_DEPTH, NUM_SEGMENTS);
	}

	/**
	 * See
	 * {@link LearnPatternSimilarityClassifier#findNearestInstanceIndex(int[][])}.
	 */
	@Test
	public void findNearestInstanceIndexTest() {
		// Prepare test data with 2 instances, NUM_TREES trees with NUM_SEGMENTS segments
		final int[][][] trainLeafNodes = new int[][][] {{{5}, {3}}, {{4}, {5}}};
		classifier.setTrainLeafNodes(trainLeafNodes);
		
		final int[][] testLeafNodes = new int[][] { { 4 }, { 4 } };
		
		Assert.assertEquals("The nearest neighbor instance test selected the wrong instance.", 1,
				classifier.findNearestInstanceIndex(testLeafNodes));
	}
}
