package jaicore.ml.clustering;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.junit.Test;

/**
 * Test class to test Gmeans clusters.
 * 
 * @author jnowack
 *
 */
public class GMeansTest {

	private static final int DATA_POINT_NUMBER = 10000;
	private static final long SEED = 145553;
	
	/**
	 * Creates random datapoints and clusters. Then creates a UI to visualize the clusters. Not a Unit test for obvious reasons. 
	 * 
	 * @param args Nothing to see here
	 */
	public static void main(String[] args) {
		Random rand = new Random(SEED);
		
		// generate random points
		ArrayList<DoublePoint> data = new ArrayList<>(DATA_POINT_NUMBER);
		for (int i = 0; i < DATA_POINT_NUMBER; i++) {
			data.add(new DoublePoint( new int[] {rand.nextInt(500), rand.nextInt(500)}));
		}
		
		// create Cluster and results
		GMeans<DoublePoint> cluster = new GMeans<>(data);
		List<CentroidCluster<DoublePoint>> result = cluster.cluster();
		
		
		// create Window
		JFrame frame = new JFrame("Simple Result UI");
		
		@SuppressWarnings("serial")
		Canvas c = new Canvas() {
			@Override
			public void paint(Graphics g) {
				// paint points colored by cluster
				for (CentroidCluster<DoublePoint> centroidCluster : result) {
					g.setColor(new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));
					for (DoublePoint point : centroidCluster.getPoints()) {
						g.fillOval((int)point.getPoint()[0]-2, (int)point.getPoint()[1]-2, 4, 4);
					}
				}
				
			}
		};
		c.setSize(500, 500);
		
		frame.getContentPane().add(c);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 500);
		frame.setVisible(true);
		
	}
	
	/**
	 * Creates a cluster and checks if output is generated without exceptions.
	 * 
	 * @throws Exception if the test fails
	 */
	@Test
	public void createClusters() throws Exception {
		Random rand = new Random(SEED);
		
		ArrayList<DoublePoint> data = new ArrayList<>(DATA_POINT_NUMBER);
		
		for (int i = 0; i < DATA_POINT_NUMBER; i++) {
			data.add(new DoublePoint( new int[] {rand.nextInt(500), rand.nextInt(500)}));
		}
		
		// create Cluster
		GMeans<DoublePoint> cluster = new GMeans<>(data);
		
		List<CentroidCluster<DoublePoint>> result = cluster.cluster();
		
		assertNotNull("GMeans created no result!", result);
		
		assertFalse("GMeans created no clusters!", result.size() == 0);
		
		for (CentroidCluster<DoublePoint> centroidCluster : result) {
			assertFalse("A Gmeans cluster is empty!", centroidCluster.getPoints().size() == 0);
		}
	}
}










