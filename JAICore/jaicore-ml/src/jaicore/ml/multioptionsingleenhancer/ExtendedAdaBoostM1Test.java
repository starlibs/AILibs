package jaicore.ml.multioptionsingleenhancer;

import org.junit.Test;

import jaicore.ml.WekaUtil;
import weka.classifiers.Classifier;

import static org.junit.Assert.*;

public class ExtendedAdaBoostM1Test {
	
	public void buildClassifier() {
		
		Classifier classifier = new ExtendedAdaBoostM1V2();
		
		assertTrue(classifier instanceof WekaUtil);
		
	}

}
