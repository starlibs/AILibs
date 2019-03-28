
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.stat.inference.TTest;

import jaicore.CustomDataTypes.Solution;
import jaicore.modifiedISAC.ClassifierRankingForGroup;
import jaicore.modifiedISAC.ModifiedISAC;
import jaicore.modifiedISAC.ModifiedISACInstanceCollector;
import jaicore.modifiedISAC.evalutation.modifiedISACEvaluator;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class Bob {
	private static void printDoubleArray(double[] d) {
		for (int i = 0; i < d.length; i++) {
			System.out.print("|" + d[i] + "|");
		}
		System.out.println(" ");
	}
	
	public static void normalRun() {
		try {
			ModifiedISACInstanceCollector coll = new ModifiedISACInstanceCollector();

			ModifiedISAC isac = new ModifiedISAC(coll, null);
			isac.bulidRanker();

			ArrayList<ClassifierRankingForGroup> rankings = isac.getRankings();
			for (ClassifierRankingForGroup rank : rankings) {
				System.out.print("Center des Clusters: ");
				printDoubleArray(rank.getIdentifierForGroup().getIdentifier());
				int tmp = 1;
				for (Solution<String> solu : rank.getRanking()) {
					System.out.println("Nummer " + tmp + " " + solu.getSolution());
					tmp++;
				}
				System.out.println("--------------------------------------------------------");
			}

		} catch (Exception e) {
			System.out.println("Hallo Hier ist ein Fehler in der main");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	static int indexForNan(double[] d) {
		int index = 0;
		for(int i = 0; i<d.length;i++) {
			if(Double.isNaN(d[i])) {
				return index;
			}
			index++;
		}
		return index;
	}

	private static double avarge (double[] d ) {
		int totalval = 0;
		double result = 0.0;
		for(int i = 0; i <d.length;i++) {
			if(!Double.isNaN(d[i])){
				result+= d[i];
				totalval++;
			}
		}
		result = result/totalval;
		return result;
	}
	public static void main(String[] args) throws Exception {
		InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("metaData_smallDataSets_computed.arff");
		DataSource source = new DataSource(inputStream);
		Instances data = source.getDataSet();
//		normalRun();
		double[] d = modifiedISACEvaluator.evaluateModifiedISACLeaveOneOut(data);
		System.out.println("-----------------------------------------------");
		System.out.println(" ");
		System.out.println("Avarage runtime "+Arrays.stream(modifiedISACEvaluator.getTimes()).average().getAsDouble()+" ms");
		System.out.println("Random forest in top 3 "+modifiedISACEvaluator.getRandomForestplatz1());
		System.out.println("Random Forest in avareg in top 3 "+modifiedISACEvaluator.getRandomForestplatz1()/data.numInstances());
		System.out.println("Avareg place of Random Forest "+ Arrays.stream(modifiedISACEvaluator.getRandomForest()).average().getAsDouble());
		
		System.out.println("Naive baise multi in top 3 "+modifiedISACEvaluator.getNaivebaismulti());
		System.out.println("Naive baise multi in avareg in top 3 "+modifiedISACEvaluator.getNaivebaismulti()/data.numInstances());
		
		System.out.println("Naive bais in top 3 "+modifiedISACEvaluator.getNaivebais());
		System.out.println("Naive bais in avareg in top 3 "+modifiedISACEvaluator.getNaivebais()/data.numInstances());
		
		System.out.println(" ");
		System.out.println("My correlation "+Arrays.toString(d));
		Variance variance = new Variance();
		
		System.out.println("Ml correlation "+Arrays.toString(modifiedISACEvaluator.getKendallforML()));
		double tmp = Arrays.stream(d).filter(x -> x != Double.NaN).average().getAsDouble();
		double tmp2 = Arrays.stream(modifiedISACEvaluator.getKendallforML()).filter(x -> x != Double.NaN).average().getAsDouble();
		System.out.println(" ");
		System.out.println("The varaince of my method "+ variance.evaluate(d,tmp));
		System.out.println("The varaince of ML method "+ variance.evaluate(modifiedISACEvaluator.getKendallforML(), tmp2));
		TTest testkendall = new TTest();
		double pvalue = testkendall.tTest(modifiedISACEvaluator.getKendallforML(),d);
		boolean testpast = testkendall.tTest(modifiedISACEvaluator.getKendallforML(),d,0.05); 
		System.out.println("Kendall correlation significanc test for ML and my method "+pvalue+" test past: "+testpast);
		System.out.println("");
		
		System.out.println("Average correlation: "+tmp);
		System.out.println("Max Kendall correlation my "+Arrays.stream(d).max().getAsDouble());
		double [] tmpmy = Arrays.copyOfRange(d, 0, d.length);
		for(int i = 0; i < d.length;i++) {
			tmpmy[i]=Math.abs(d[i]);
		}
		double min = Arrays.stream(tmpmy).min().getAsDouble();
		System.out.println("Min Kendall correlation my "+min);
		int indexOfMin = 0;
		for(int i = 0;i<tmpmy.length;i++) {
			if(tmpmy[i] == min) {
				indexOfMin = i;
			}
		}
		System.out.println("Index of minimum "+(indexOfMin+1));
		System.out.println(" ");
		
		double[] dml = modifiedISACEvaluator.getKendallforML();
		double tmpml = Arrays.stream(dml).filter(x -> x != Double.NaN).average().getAsDouble();
		
		System.out.println("Max Kendall correlation of ML "+Arrays.stream(dml).max().getAsDouble());
		System.out.println("Average correlation "+tmpml);
		double [] tmpdml = Arrays.copyOfRange(dml, 0, dml.length);
		for(int i = 0; i < dml.length;i++) {
			tmpdml[i]=Math.abs(dml[i]);
		}
		System.out.println("Min Kendall correlation of ML "+Arrays.stream(tmpdml).min().getAsDouble());
		System.out.println(" ");
		
		
		System.out.println("My first place vs. opt first place in acc "+Arrays.toString(modifiedISACEvaluator.getPlatz1my()));
		System.out.println("The overall first place vs. opt first place in acc "+Arrays.toString(modifiedISACEvaluator.getPlatz1overall()));
		System.out.println("The ml static ranking vs. opt first place in acc "+Arrays.toString(modifiedISACEvaluator.getPlatz1ml()));
		System.out.println(" ");
		
		
		System.out.println("The avrage of my method vs opt acc difference "+avarge(modifiedISACEvaluator.getPlatz1my()));	
		System.out.println("The avrage of the overall vs opt acc difference "+avarge(modifiedISACEvaluator.getPlatz1overall()));
		System.out.println("The avrage of Ml vs opt acc difference "+avarge(modifiedISACEvaluator.getPlatz1ml()));
		System.out.println(" ");
		
		Arrays.sort(modifiedISACEvaluator.gettop3mymethod());
		System.out.println("my top3 vs opt max difference "+modifiedISACEvaluator.gettop3mymethod()[indexForNan(modifiedISACEvaluator.gettop3mymethod())-1]);
		Arrays.sort(modifiedISACEvaluator.getTop3overall());
		System.out.println("overall top3 vs opt max difference "+modifiedISACEvaluator.getTop3overall()[indexForNan(modifiedISACEvaluator.getTop3overall())-1]);
		Arrays.sort(modifiedISACEvaluator.getTop3ml());
		System.out.println("Ml top3 vs opt max difference "+modifiedISACEvaluator.getTop3ml()[indexForNan(modifiedISACEvaluator.getTop3ml())-1]);
		System.out.println(" ");
		
		System.out.println("The avarge of the top3 of my mehtod vs opt acc difference " +avarge(modifiedISACEvaluator.gettop3mymethod()));
		System.out.println("The avarge of the top3 of the baseline vs opt acc difference " +avarge(modifiedISACEvaluator.getTop3overall()));
		System.out.println("The avarge of the top3 of ML vs opt acc difference " +avarge(modifiedISACEvaluator.getTop3ml()));
		System.out.println(" ");
		
		Arrays.sort(modifiedISACEvaluator.getPlatz1my());
		double[] input1 = Arrays.copyOfRange(modifiedISACEvaluator.getPlatz1my(), 0, indexForNan(modifiedISACEvaluator.getPlatz1my()));
		System.out.println("The max difference to the opt from my method "+ Arrays.stream(input1).max().getAsDouble());
		
		
		Arrays.sort(modifiedISACEvaluator.getPlatz1overall());
		double[] input2 = Arrays.copyOfRange(modifiedISACEvaluator.getPlatz1overall(), 0, indexForNan(modifiedISACEvaluator.getPlatz1overall()));
		System.out.println("The max difference of the baseline to opt "+ Arrays.stream(input2).max().getAsDouble());
		
		Arrays.sort(modifiedISACEvaluator.getPlatz1ml());
		double[] input3 = Arrays.copyOfRange(modifiedISACEvaluator.getPlatz1ml(), 0, indexForNan(modifiedISACEvaluator.getPlatz1ml()));
		System.out.println("The max difference of ML-plan to opt "+ Arrays.stream(input3).max().getAsDouble());
		
		Arrays.sort(modifiedISACEvaluator.getUntochedmy());
		Arrays.sort(modifiedISACEvaluator.getUntocedoverall());
		Arrays.sort(modifiedISACEvaluator.getUntouchedml());

		double[] sample1 = Arrays.copyOfRange(modifiedISACEvaluator.getUntocedoverall(), 0, indexForNan(modifiedISACEvaluator.getUntocedoverall()));
		double[] sample2 = Arrays.copyOfRange(modifiedISACEvaluator.getUntochedmy(), 0, indexForNan(modifiedISACEvaluator.getUntochedmy()));
		double[] sample3 = Arrays.copyOfRange(modifiedISACEvaluator.getUntouchedml(), 0, indexForNan(modifiedISACEvaluator.getUntouchedml()));
		
		System.out.println("Steps it takes in avarage to reach the optimal solution in my mehtod "+Arrays.stream(modifiedISACEvaluator.getStepdifference()).average().getAsDouble());
		System.out.println("Steps til ml-plan reaches the opt solution "+Arrays.stream(modifiedISACEvaluator.getStepdifferenceML()).average().getAsDouble());
		TTest test = new TTest();
		System.out.println("Der signifinztest von meiner methode und der baseline "+test.tTest(sample1, sample2));
		
		TTest test2 = new TTest();
		System.out.println("Der signifinztest von meiner methode und der Methode von ML-Plan "+test2.tTest(sample3, sample2));
		
		
		//printDoubleArray(d);
	}

}
