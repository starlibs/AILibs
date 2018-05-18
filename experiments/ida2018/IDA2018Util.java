package ida2018;

import jaicore.basic.MySQLAdapter;

public class IDA2018Util {
	public static MySQLAdapter getAdapter() {
		return new MySQLAdapter("isys-db.cs.upb.de", "ida2018", "WsFg33sE6aghabMr", "results_reduction");
	}
	
	public static String[] getConsideredDatasets() {
		return new String[] {
				"audiology.arff",
				"autoUnivau6750.arff",
				"car.arff",
				"cnae9.arff",
				"fbis.wc.arff",
				"kropt.arff",
				"letter.arff",
				"mfeat-factors.arff",
				"mfeat-fourier.arff",
				"mfeat-karhunen.arff",
				"mfeat-pixel.arff",
				"optdigits.arff",
				"pendigits.arff",
				"page-blocks.arff",
				"segment.arff",
				"semeion.arff",
				"vowel.arff",
				"waveform.arff",
				"winequality.arff",
				"yeast.arff",
				"zoo.arff"
				
		};
	}
}
