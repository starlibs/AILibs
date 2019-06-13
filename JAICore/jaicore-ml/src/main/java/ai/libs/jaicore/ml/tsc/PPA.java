package ai.libs.jaicore.ml.tsc;

// source https://jmotif.github.io/sax-vsm_site/morea/algorithm/PAA.html

public class PPA {

	private PPA() {
		/* no instantiation allowed */
	}

	public static double[] ppa(final double[] input, final int lengthM) {
		double[] ppa = new double[lengthM];
		double n = input.length;
		for (int i = 0; i < lengthM; i++) {
			double ppavalue = 0;
			for (int j = (int) (n / ((lengthM * (i - 1)) + 1)); j < ((n / lengthM) * i); j++) {
				ppavalue += input[j];
			}
			ppavalue = (lengthM / n) * ppavalue;
			ppa[i] = ppavalue;
		}
		return ppa;
	}
}
