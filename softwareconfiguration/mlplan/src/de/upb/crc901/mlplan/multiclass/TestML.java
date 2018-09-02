package de.upb.crc901.mlplan.multiclass;

import weka.classifiers.Classifier;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.Vote;
import weka.classifiers.trees.J48;

public class TestML {
	
	public static void main(String[] args) throws Exception {
		
		Vote cla = new Vote();
		//AdaBoostM1 cla = new AdaBoostM1();
		
		//String[] options = {"-I", "20", "-W", "weka.classifiers.trees.J48", "--", "-C", "0.1"};
		//String[] options = {"-B", "weka.classifiers.trees.J48", "-B", "weka.classifiers.trees.J48"};
		String[] options = {"-B", "weka.classifiers.trees.J48 -M 5"};
		//String[] options = {"-S", "2"};
		
		cla.setOptions(options);
		for(String s: cla.getOptions())
		{
			System.out.println(s);
		}
	}

}

