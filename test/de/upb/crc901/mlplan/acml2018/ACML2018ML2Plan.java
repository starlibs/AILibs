package de.upb.crc901.mlplan.acml2018;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import de.upb.crc901.mlplan.multilabel.ML2Plan;
import de.upb.crc901.mlplan.multilabel.MultiLabelMySQLHandle;
import jaicore.ml.WekaUtil;
import jaicore.ml.multilabel.evaluators.F1AverageMultilabelEvaluator;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.MLUtils;
import weka.core.Instances;

public class ACML2018ML2Plan {

	public static void main(String[] args) {
		try {
			List<String> datasets = Arrays.asList(new File(args[0]).listFiles()).stream().filter(f -> f.isFile()).map(f -> f.getAbsolutePath()).collect(Collectors.toList());
			int seed = (int) Math.ceil(Math.random() * 25);
			Collections.shuffle(datasets);
			
			/* read data */
			Instances data = new Instances(new BufferedReader(new FileReader(new File(datasets.get(0)))));
			Collections.shuffle(data);
			try {
				MLUtils.prepareData(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("Evaluating dataset: " + data.relationName());
			Collection<Integer>[] overallSplitDecision = WekaUtil.getArbitrarySplit(data, new Random(seed), .7f);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < data.size(); i++) {
				sb.append(overallSplitDecision[0].contains(i) ? "1" : "0");
			}
			List<Instances> outerSplit = WekaUtil.realizeSplit(data, overallSplitDecision);
			
			/* configure ml2plan */
			MultiLabelMySQLHandle handle = new MultiLabelMySQLHandle("isys-db.cs.upb.de", "mlplan", "UMJXI4WlNqbS968X", "acml2018");
			ML2Plan ml2plan = new ML2Plan();
			ml2plan.setNumberOfCPUs(Integer.valueOf(args[2]));
			ml2plan.setTimeout(Integer.valueOf(args[1]));
			ml2plan.setMemory(4 * 1024 + (int)(Runtime.getRuntime().maxMemory() / 1024 / 1024));
			ml2plan.setRandomSeed(seed);
			ml2plan.buildClassifier(outerSplit.get(0));
			
			/* evaluate solution */
			MultiLabelClassifier chosenClassifier = ml2plan.getSelectedClassifier();
			double loss = new F1AverageMultilabelEvaluator(new Random(seed)).loss(chosenClassifier, outerSplit.get(1));
			System.out.println(loss);
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
