package de.upb.crc901.mlplan.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

import org.junit.Test;

import de.upb.crc901.mlplan.multiclass.classifiers.MCTSPipelineSearcher;
import weka.core.Instances;

public class MCTSSearch {

	@Test
	public void test() throws Exception {
		
		/* read in data */
		final String dataset = "autowekasets/yeast";
		System.out.print("Reading in data ...");
		Instances data = new Instances(new BufferedReader(new FileReader("testrsc/" + dataset + "/train.arff")));
		data.addAll(new Instances(new BufferedReader(new FileReader("testrsc/" + dataset + "/test.arff"))));
		System.out.println("Done");
		data.setClassIndex(data.numAttributes() - 1);
		
		/* setup search */
		MCTSPipelineSearcher searcher = new MCTSPipelineSearcher(new Random(1), 1000 * 60, false);
		searcher.buildClassifier(data);
	}

}
