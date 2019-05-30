package jaicore.ml.tsc;


//source https://jmotif.github.io/sax-vsm_site/morea/algorithm/PAA.html

public class PPA {

	public static double[] ppa(double[] input, int lengthM ) {
		double[] ppa = new double[lengthM];
		double n = input.length;
		
		//TODO if I find out if n/m is an int i change this must be Int 
		//TODO how to chose break points for alphabet equal ? 
		//TODO if ppa length == timeseries legth return ts
		for(int i = 0; i < lengthM; i++) {
			double ppavalue = 0;
			for(int j = (int) (n/((lengthM*(i-1))+1)); j<((n/lengthM)*i); j++) {
				ppavalue+= input[j];
			}
			ppavalue = (lengthM/n)*ppavalue;
			ppa[i]=ppavalue;
		}
		
		return ppa;
		
	}
}
