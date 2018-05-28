package ida2018;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import jaicore.basic.SQLAdapter;
import jaicore.basic.SetUtil;
import jaicore.ml.WekaUtil;

public class IDA2018Util {
	public static SQLAdapter getAdapter() {
		return new SQLAdapter("isys-db.cs.upb.de", "ida2018", "WsFg33sE6aghabMr", "results_reduction");
	}
	
	public static Collection<String> getConsideredDatasets() {
		return Arrays.asList(new String[] {
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
				
		});
	}
	
	public static Collection<String> getConsideredLearners() {
		Collection<String> learners = WekaUtil.getBasicLearners();
		Collection<String> blackList = Arrays.asList(new String[] {
			"reptree",
			"votedperceptron",
			"oner",
			"zeror"
		});
		learners.removeIf(l -> {
			for (String blocked : blackList) {
				if (l.toLowerCase().contains(blocked))
					return true;
			}
			return false;
		});
		return learners;
	}
	
	public static Collection<List<String>> getReductionStumpCombinations() {
		Collection<String> classifiers = getConsideredLearners();
		Collection<List<String>> classifierCombos;
		try {
			classifierCombos = SetUtil.cartesianProduct(classifiers, 3);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
		return classifierCombos;
	}
}
