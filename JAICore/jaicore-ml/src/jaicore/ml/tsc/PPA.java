package jaicore.ml.tsc;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

//source https://jmotif.github.io/sax-vsm_site/morea/algorithm/PAA.html

public class PPA {

	public static INDArray ppa(INDArray input, int lengthM ) {
		INDArray ppa = Nd4j.zeros(lengthM);
		double n = input.length();
		
		//TODO if I find out if n/m is an int i change this must be Int 
		//TODO how to chose break points for alphabet equal ? 
		//TODO if ppa length == timeseries legth return ts
		for(int i = 0; i < lengthM; i++) {
			double ppavalue = 0;
			for(int j = (int) (n/((lengthM*(i-1))+1)); j<((n/lengthM)*i); j++) {
				ppavalue+= input.getDouble(j);
			}
			ppavalue = (lengthM/n)*ppavalue;
			ppa.putScalar(i,ppavalue);
		}
		
		return ppa;
		
	}
}
