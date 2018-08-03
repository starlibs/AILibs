package defaultEval.core.experiment;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.WekaUtil;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeSelection;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * Little Helper Class to generate a string of searcher and evaluator combinations to use in setup.properties
 * 
 * @author Joshua
 *
 */
public class PreprocessorListHelper {

	
	
	public static void main(String[] args) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		int i = 0;
		for (Pair<String, String> s : WekaUtil.getValidPreprocessorCombinations()) {
			boolean failed = false;
			try {
				// check combination
				AttributeSelection as = new AttributeSelection();
				System.out.println(s.getX());
				as.setSearch(ASSearch.forName(s.getX(), new String[] {}));
				System.out.println(s.getY());
				as.setEvaluator(ASEvaluation.forName(s.getY(), new String[] {}));
				
				DataSource ds;
				Instances instances = null;
				try {
					ds = new DataSource("F:\\Data\\Uni\\PG\\DefaultEvalEnvironment\\datasets\\breast-cancer.arff"); // use your own
					instances = new Instances(ds.getDataSet());
					instances.setClassIndex(instances.numAttributes()-1);
				} catch (Exception e) {
					e.printStackTrace(System.out);
				}
				
				as.SelectAttributes(instances);
				System.out.println("Status: OK");
				i++;
			} catch (Exception | Error e) {
				System.out.println("Status: FAIL");
				System.out.println(e.toString());
				failed = true;
			}
			System.out.println("");
			
			
			if(!failed) {
				if(!first) {
					sb.append(",");
				}
				first = false;
				sb.append(s.getX());
				sb.append(";");
				sb.append(s.getY());
			}
		}
		
		System.out.println(sb.toString());
		System.out.println("NUMBER: " + i);
	}
	
}
