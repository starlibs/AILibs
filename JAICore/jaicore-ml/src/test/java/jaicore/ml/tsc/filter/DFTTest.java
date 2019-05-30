package jaicore.ml.tsc.filter;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;


import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

/**
 * @author Helen
 * DFT JUnit test 
 *
 */
@RunWith(JUnit4.class)
public class DFTTest {
	double[] timeseries1;
	double[] timeseries2;
	
	TimeSeriesDataset dataset;
	
	@Before
	public void setup() {
		timeseries1 = new double [] {1,1,1,1,1,1,1,1};
		timeseries2 = new double[] {1,2,4,3,5,2,4,3};
		double[][] matrix = new double[3][8];
		matrix[0] = timeseries1;
		matrix[1] = timeseries2;
		
		ArrayList<double[][]> futureDataSet = new ArrayList<double[][]>();
		futureDataSet.add(matrix);
		dataset = new TimeSeriesDataset(futureDataSet,null, null);
	}
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void testFit() throws IllegalArgumentException, NoneFittedFilterExeception {
		DFT testDFT = new DFT();
		//testDFT.setMeanCorrected(true);
//		testDFT.setNumberOfDisieredCoefficients(7);
		testDFT.setNumberOfDisieredCoefficients(2);
		testDFT.fit(dataset);
		TimeSeriesDataset output = null;
//		thrown.expect(IllegalArgumentException.class);
		try {
			SlidingWindowBuilder slide = new SlidingWindowBuilder();
			slide.setDefaultWindowSize(3);
			for(double[][] matrix : dataset.getValueMatrices()) {
				for(double[] instance : matrix) {
					TimeSeriesDataset tmp2= testDFT.rekursivDFT(slide.specialFitTransform(instance));
					for(double[][] m : tmp2.getValueMatrices()) {
						for(double[] i : m) {
							System.out.println(Arrays.toString(i));
							System.out.println("------------------------------------------------");
						}
						System.out.println("------------------------------------------------");
					}
					System.out.println("------------------------------------------------");
				}
			}
			output = (TimeSeriesDataset)testDFT.transform(dataset);
			System.out.println("iterativ");
			System.out.println("------------------------------------------------");
			
			for(double[][] m : output.getValueMatrices()) {
				for(double[] i : m) {
					System.out.println(Arrays.toString(i));
				}
			}
			
			System.out.println("iterativ slides");
			System.out.println("------------------------------------------------");
			for(double[][] matrix : dataset.getValueMatrices()) {
				for(double[] instance : matrix) {
					TimeSeriesDataset tmp2= testDFT.fitTransform((slide.specialFitTransform(instance)));
					System.out.println(tmp2.getNumberOfVariables());
					for(double[][] m : tmp2.getValueMatrices()) {
						for(double[] i : m) {
							System.out.println(Arrays.toString(i));
							System.out.println("------------------------------------------------");
						}
						System.out.println("-----------hallo-------------------------------------");
					}
					System.out.println("-----------DataSetFinished------------------------------------");
				}
			}
			
			assertEquals(1,output.getValues(0)[0][0], 1.0E-5);
			assertEquals(0, output.getValues(0)[0][1], 1.0E-5);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoneFittedFilterExeception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
