package jaicore.modifiedISAC.evalutation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;

import jaicore.CustomDataTypes.RankingForGroup;
import jaicore.CustomDataTypes.Solution;
import jaicore.modifiedISAC.ModifiedISAC;
import jaicore.modifiedISAC.ModifiedISACInstanceCollector;
import java_cup.symbol;
import weka.core.Instance;
import weka.core.Instances;

public class modifiedISACEvaluator {
	private static double [] platz1my;
	private static double [] top3mymethod;
	private static double [] platz1overall;
	private static double [] top3overall;
	private static double[] untochedmy;
	private static double[] untocedoverall;
	private static double[] platz1ml;
	private static double[] top3ml;
	private static double[] untouchedml;
	private static double[] stepdifference;
	private static double[] stepdifferenceML;
	private static double[] kendallforML;
	private static double[] times;
	private static double randomForestplatz1;
	private static double[] randomForest;
	private static double naivebaismulti;
	private static double naivebais;
	
	public static double getNaivebais() {
		return naivebais;
	}
	public static double getNaivebaismulti() {
		return naivebaismulti;
	}
	public static double[] getRandomForest() {
		return randomForest;
	}
	public static double getRandomForestplatz1() {
		return randomForestplatz1;
	}
	public static double[] getTimes() {
		return times;
	}
	public static double[] getKendallforML() {
		return kendallforML;
	}
	public static double[] getStepdifferenceML() {
		return stepdifferenceML;
	}
	public static double[] getStepdifference() {
		return stepdifference;
	}
	public static double[] getPlatz1ml() {
		return platz1ml;
	}
	public static double[] getTop3ml() {
		return top3ml;
	}
	public static double[] getUntouchedml() {
		return untouchedml;
	}
	public static double[] getUntochedmy() {
		return untochedmy;
	}
	public static double[] getUntocedoverall() {
		return untocedoverall;
	}
	public static double[] getPlatz1my() {
		return platz1my;
	}
	public static double[] getPlatz1overall() {
		return platz1overall;
	}
	
	public static double[] gettop3mymethod() {
		return top3mymethod;
	}
	public static double[] getTop3overall() {
		return top3overall;
	}
	
	public static double[] evaluateModifiedISACLeaveOneOut(Instances data) {
		double[] results = new double[data.numInstances()];
		platz1my = new double[data.numInstances()];
		platz1overall= new double[data.numInstances()];
		top3mymethod= new double[data.numInstances()];
		top3overall= new double[data.numInstances()];
		untochedmy= new double[data.numInstances()];
		untocedoverall= new double[data.numInstances()];
		top3ml = new double[data.numInstances()];
		untouchedml = new double[data.numInstances()];
		platz1ml = new double[data.numInstances()];
		stepdifference = new double[data.numInstances()];
		stepdifferenceML = new double[data.numInstances()];
		kendallforML = new double[data.numInstances()];
		times =  new double[data.numInstances()];
		randomForest = new double[data.numInstances()];
		naivebaismulti = 0;
		naivebais = 0;
		
		for (int i = 0; i < data.numInstances(); i++) {
			HashMap<String, Integer> positionInRanking = new HashMap<String, Integer>();
			Instances trainingData = new Instances(data);
			Instances testprep = new Instances(data);
			Instances tester = new Instances(data);
			Instance reminder = tester.get(i);
			
			trainingData.delete(i);
			
			double[] overallranking = new double[22];
			int[] totalvalue = new int[22];

			for(Instance inst : trainingData) {
				int tmp = 0;
				for(int l = inst.numAttributes()-1; l >= 104;l--) {
					if(!Double.isNaN(inst.value(l))){
						overallranking[tmp]+= inst.value(l);
						totalvalue[tmp]++;
					}	
					tmp++;
				}
			}
			
			for(int o = 0; o<overallranking.length;o++) {
				overallranking[o] = overallranking[o]/totalvalue[o];
			}
			
			HashMap<String,Double> overall = new HashMap<String,Double>();
			
			int exampel = 0;
			if(i == 0) {
				exampel = 1;
			}
			
			for(int p = 0; p<overallranking.length;p++) {
				overall.put(data.get(exampel).attribute((data.numAttributes()-1)-p).name(),overallranking[p]);
			}
			StopWatch watch = new StopWatch();
			watch.start();
			ModifiedISACInstanceCollector collect = new ModifiedISACInstanceCollector(trainingData, 104, 125);
			ModifiedISAC isac = new ModifiedISAC(collect, null);
			isac.bulidRanker();
			watch.stop();
			System.out.println(watch.getTime()+" ms");
			times[i] = watch.getTime();
			watch.reset();

			HashMap<String, Double> ClassandPerfo = new HashMap<String, Double>();
			for (int p = testprep.numAttributes() - 1; p >= 104; p--) {
				ClassandPerfo.put(testprep.get(i).attribute(p).name(), testprep.get(i).value(p));
				testprep.deleteAttributeAt(p);
			}
			testprep.deleteAttributeAt(0);
			Instance inst = testprep.get(i);
			
			
			RankingForGroup<double[], String> ranking = isac.getRanking(inst);
			// get the ground truth ranking as string list
			
			int size = 3;
			
			
			double[] rankingTruth = new double[22];
			int tmp = 0;
			ArrayList<String> top3truth = new ArrayList<String>();
			
			while (!ClassandPerfo.isEmpty()) {
//				System.out.println("size left " + ClassandPerfo.size());
				double maxPerfo = Double.MIN_VALUE;
				String myClassi = "";
				for (String classi : ClassandPerfo.keySet()) {
					if (!ClassandPerfo.get(classi).isNaN()) {
//						System.out.println("double nicht nan");
						if (ClassandPerfo.get(classi) >= maxPerfo) {
							maxPerfo = ClassandPerfo.get(classi);
							myClassi = classi;
						}
					}
					
				}
//				System.out.println("myClassi is null " + myClassi == null);
				if (myClassi.isEmpty()) {
					int nans = tmp;
					for (String str : ClassandPerfo.keySet()) {
						if(nans<size) {
							top3truth.add(str);
						}
						positionInRanking.put(str, nans);
						rankingTruth[nans] = nans;
						nans++;
					}
					ClassandPerfo.clear();
				} else {
//					System.out.println(tmp);
					if(tmp< size) {
						top3truth.add(myClassi);
						if(myClassi.equals("weka.classifiers.trees.RandomForest")) {
							randomForestplatz1++;
						}
						if(myClassi.equals("weka.classifiers.bayes.NaiveBayesMultinomial")) {
							naivebaismulti++;
						}
						if(myClassi.equals("weka.classifiers.bayes.NaiveBayes")) {
							naivebais++;
						}
					}
					rankingTruth[tmp] = tmp;
					positionInRanking.put(myClassi, tmp);
					ClassandPerfo.remove(myClassi);
					tmp++;
				}
				
			}
			
			System.out.println("Ranking truth "+Arrays.toString(rankingTruth));

			double[] difference3 = new double[size];
			
			HashMap<String,Double> loopoverall = (HashMap<String, Double>) overall.clone();
			double finishedoverallranking[] = new double[22];
			HashMap<String,Integer> rankingoverall = new HashMap<String,Integer>();
			int loopcounter = 0;
			
			while(!loopoverall.isEmpty()) {
				double maxPerfo = Double.MIN_VALUE;
				String myClassifier = "";
				for(String calssi: loopoverall.keySet()) {
					if(loopoverall.get(calssi)>=maxPerfo) {
						maxPerfo = loopoverall.get(calssi);
						myClassifier = calssi;
					}
				}
				if(loopcounter<size) {
					if(maxPerfo != Double.MIN_VALUE) {
						difference3[loopcounter]=maxPerfo;
						
					}
				}
				finishedoverallranking[positionInRanking.get(myClassifier)] = loopcounter;
				rankingoverall.put(myClassifier, loopcounter);
				loopoverall.remove(myClassifier);
				loopcounter++;
			}
			System.out.println("baseline ranking "+Arrays.toString(finishedoverallranking));
			// get the ranking as string list
			ArrayList<String> rankingAsStringList = new ArrayList<String>();
			for (Solution<String> rank : ranking.getRanking()) {
				rankingAsStringList.add(rank.getSolution());
			}
			
			double[] rankingFromMyMethod = new double[22];
			ArrayList<String> top3my = new ArrayList<String>();
			int intermidiate = 0;
			for (String classi : rankingAsStringList) {
				rankingFromMyMethod[positionInRanking.get(classi)] = intermidiate;
				if(intermidiate<size) {
					top3my.add(classi);
				}
//				rankingFromMyMethod[intermidiate] = rankingoverall.get(classi);
				intermidiate++;
				
			}
			System.out.println("My rnaking "+ Arrays.toString(rankingFromMyMethod));
//			
			ArrayList<String> top3MlPlan = new ArrayList<String>();
			ArrayList<String> mlPlanranking = makeStaticRanking();
			double[] mlplanranking = new double[22];
			intermidiate = 0;
			
			for(String str : mlPlanranking) {
				Integer perfo = null;
				for(String keys : positionInRanking.keySet()) {
					if(keys.equals(str)) {
						perfo = positionInRanking.get(keys);
					}
				}
				if(perfo != null) {
					if(intermidiate<size) {
						top3MlPlan.add(str);
					}
					int index = (int) perfo;
					mlplanranking[index] =intermidiate;
					intermidiate++;
				}	
				
			}
			System.out.println("ML-plan ranking "+Arrays.toString(mlplanranking));
			
			double stpestiloptwouldbereached = 0;
			double stepstillmlreachopt = 0;
			stpestiloptwouldbereached = rankingFromMyMethod[0]+1;
			stepstillmlreachopt = mlplanranking[0]+1;
		
			double[] difference1 = new double[size];
			double[] difference2 = new double[size];
			double[] difference4 = new double[size];
			
			for(int h = 0; h<size;h++) {
				String classitruth = top3truth.get(h);
				String mltruth = top3MlPlan.get(h);
				String mytruth = top3my.get(h);
				
				double perfotruth = 0;
				double perfomy = 0;	
				double perfoml = 0;
				
				for(int t = 125; t>=104;t--) {
					if(reminder.attribute(t).name().equals(classitruth)) {
						perfotruth = reminder.value(t);
					}
					if(reminder.attribute(t).name().equals(mytruth)) {
						perfomy = reminder.value(t);
					}
					if(reminder.attribute(t).name().equals(mltruth)) {
						perfoml = reminder.value(t);
					}
				}
				difference1[h] = perfotruth;
				difference2[h] = perfomy;
				difference4[h] = perfoml;
			}
			
			
			
			System.out.println("Das betrachtete Datenset: "+(i+1));
			
			System.out.println("Die steps die es bräuchte um Platz eins der opt lösung in meiner zu erreichen "+stpestiloptwouldbereached);
			stepdifference[i] = stpestiloptwouldbereached;
			stepdifferenceML[i] = stepstillmlreachopt;
			
			System.out.print("Der Verlust zweichen Platz eins der optimal Lösung und der besten meiner Lösungen: ");
			System.out.println((Math.rint((1000.0 *(difference1[0]-difference2[0]))))/1000.0);
			System.out.println("Mein Platz 3: "+difference2[size-1]);
			System.out.print("Der Verlust gegen meinen Platz 3: ");
			System.out.println((Math.rint((1000.0 *(difference1[0]-difference2[size-1]))))/1000.0);
			untochedmy[i]= difference2[0];
			untocedoverall[i] = difference3[0];
			untouchedml[i] = difference4[0];
			System.out.println(difference2[0]+" "+difference4[0]);
			
			System.out.println("Bester in der optimalen Lösung: "+difference1[0]);
			System.out.println("Mein Platz eins:  "+difference2[0]);
			
			double platz1 = difference2[0];
			platz1my[i] = difference1[0]-platz1;
			platz1ml[i] = difference1[0]-difference4[0];
			System.out.println("Performance von ML platz 1 "+difference4[0]);
			
			System.out.println("Platz eins der baseline "+difference3[0]);
			platz1overall[i] = difference1[0]-difference3[0];
			
			System.out.println("Das wahre ranking: "+top3truth.toString());
			System.out.println("Mein ranking: "+top3my.toString());
			System.out.println("ML-Plan ranking "+ top3MlPlan.toString());
			
			Arrays.sort(difference2);
			System.out.println("Beste Performance "+difference2[size-1]);
			
			System.out.println("Ist die beste Performance Plazt 1 bei meinem ranking? "+(platz1 == difference2[size-1]));
			
//			System.out.println(Arrays.toString(rankingtruth));
//			System.out.println(Arrays.toString(myranking));
			
			System.out.println(" ");
			KendallsCorrelation correl = new KendallsCorrelation();
			KendallsCorrelation correlML = new KendallsCorrelation();
			results[i] = correl.correlation(rankingTruth, rankingFromMyMethod);
//			results[i] = correl.correlation(rankingTruth, finishedoverallranking);
			kendallforML[i] = correlML.correlation(rankingTruth, mlplanranking);
			
			
			Arrays.sort(difference3);
			Arrays.sort(difference4);
			top3ml[i] = difference1[0]- difference4[difference4.length-1];
			top3mymethod[i] = difference1[0]-difference2[difference2.length-1];
			top3overall[i] = difference1[0]-difference3[difference3.length-1];
			double place = 0;
			for(String str : positionInRanking.keySet()) {
				if(str.equals("weka.classifiers.trees.RandomForest")) {
					place =positionInRanking.get(str); 
				}
			}	
			randomForest[i] = place;
			
			//System.out.println("Durchlauf nummer "+i);		
		}
		return results;
	}
	
	private static ArrayList<String> makeStaticRanking(){
		ArrayList<String> staticranking = new ArrayList<String>();
		staticranking.add("weka.classifiers.trees.RandomForest");
		staticranking.add("weka.classifiers.bayes.NaiveBayesMultinomial");
		staticranking.add("weka.classifiers.bayes.NaiveBayes");
		staticranking.add("weka.classifiers.functions.SMO");
		staticranking.add("weka.classifiers.trees.RandomTree");
		staticranking.add("weka.classifiers.lazy.IBk");
		staticranking.add("weka.classifiers.trees.J48");
		staticranking.add("weka.classifiers.functions.VotedPerceptron");
		staticranking.add("weka.classifiers.functions.SimpleLogistic");
		staticranking.add("weka.classifiers.functions.Logistic");
		staticranking.add("weka.classifiers.functions.MultilayerPerceptron");
		staticranking.add("weka.classifiers.bayes.BayesNet");
		staticranking.add("weka.classifiers.functions.SGD");
		staticranking.add("weka.classifiers.trees.LMT");
		staticranking.add("weka.classifiers.lazy.KStar");
		staticranking.add("weka.classifiers.rules.JRip");
		staticranking.add("weka.classifiers.rules.PART");
		staticranking.add("weka.classifiers.trees.REPTree");
		staticranking.add("weka.classifiers.trees.DecisionStump");
		staticranking.add("weka.classifiers.meta.AdaBoostM1");
		staticranking.add("weka.classifiers.meta.AdditiveRegression");
		staticranking.add("weka.classifiers.meta.Bagging");
		staticranking.add("weka.classifiers.meta.ClassificationViaRegression");
		staticranking.add("weka.classifiers.meta.LogitBoost");
		staticranking.add("weka.classifiers.meta.MultiClassClassifier");
		staticranking.add("weka.classifiers.meta.RandomCommittee");
		staticranking.add("weka.classifiers.meta.RandomSubspace");
		staticranking.add("weka.classifiers.meta.Stacking");
		staticranking.add("weka.classifiers.meta.Vote");
		staticranking.add("weka.classifiers.functions.SimpleLinearRegression");
		staticranking.add("weka.classifiers.rules.M5Rules");
		staticranking.add("weka.classifiers.trees.M5P");
		
		return staticranking;
		
	}
}
