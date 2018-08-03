package defaultEval.core.experiment;

import jaicore.ml.WekaUtil;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class ClassifiersHelper {
	
	public static void main(String[] args) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String string : WekaUtil.getBasicLearners()) {
			System.out.println(string);
			
			
			DataSource ds;
			Instances instances = null;
			try {
				ds = new DataSource("F:\\Data\\Uni\\PG\\DefaultEvalEnvironment\\datasets\\breast-cancer.arff"); // use your own
				instances = new Instances(ds.getDataSet());
				instances.setClassIndex(instances.numAttributes()-1);
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
			
			try {
				Classifier c = AbstractClassifier.forName(string, new String[] {});
				c.buildClassifier(instances);
				
				System.out.println("OK");
				
				sb.append(string);
				sb.append(", ");
				i++;
			} catch (Exception | Error e) {
				System.out.println("FAIL " + e.getMessage());
			}

			System.out.println();
		}
		
		System.out.println();
		System.out.println(sb.toString());
		System.out.println("NUMBER: " + i);
	}
}
