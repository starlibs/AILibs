package jaicore.ml.dyadranking.loss;

import org.junit.Test;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import jaicore.ml.dyadranking.Dyad;

public class KendallsTauOfTopKTest {

	@Test
	public void test() {

		double inst1[] = {1,2,3};
		double alt1[] = {1,3,9};
		double inst2[] = {4,7,7};
		double alt2[] = {4,5,6};
		double inst3[] = {4,5,2};
		double alt3[] = {4,8,7};
		double inst4[] = {1,7,5};
		double alt4[] = {1,7,2};
		Dyad dyad1 = new Dyad(new DenseDoubleVector(inst1), new DenseDoubleVector(alt1));
		Dyad dyad2 = new Dyad(new DenseDoubleVector(inst2), new DenseDoubleVector(alt2));
		Dyad dyad3 = new Dyad(new DenseDoubleVector(inst3), new DenseDoubleVector(alt3));
		Dyad dyad4 = new Dyad(new DenseDoubleVector(inst4), new DenseDoubleVector(alt4));
		
//		Case 1
		List<Dyad>

//		Case 2
		

//		Case 3
		

//		Case 4
	
	}

}
