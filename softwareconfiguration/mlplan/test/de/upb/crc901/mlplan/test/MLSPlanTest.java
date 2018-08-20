package de.upb.crc901.mlplan.test;

import de.upb.crc901.mlplan.multiclass.DefaultPreorder;
import de.upb.crc901.mlplan.multiclass.MLPlanWEKAClassifier;

import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.ml.WekaUtil;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.graphgenerators.task.tfd.TFDTooltipGenerator;
import jaicore.search.structure.core.Node;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import weka.classifiers.Evaluation;
import weka.core.Instances;

public class MLSPlanTest {

  //@Test
//  public void test() throws Exception {
//
//    /* read data and split */
//    Instances data = new Instances(new BufferedReader(new FileReader(new File("../datasets/classification/multi-class/segment.arff"))));
//    data.setClassIndex(data.numAttributes() - 1);
//    List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(0), .7f);
//
//    MLPlan mlplan = new MLPlan();
//    // mlplan.registerListener(new HASCOSQLEventLogger<>(new MySQLAdapter("hsqldb:hsql", "localhost",
//    // "SA", "", "testdb", new Properties())));
//    mlplan.setTimeout(5);
//    mlplan.setPortionOfDataForPhase2(.3f);
//    mlplan.setNodeEvaluator(new DefaultPreorder());
//
//    new SimpleGraphVisualizationWindow<Node<TFDNode, Double>>(mlplan).getPanel().setTooltipGenerator(new TFDTooltipGenerator<>());
//
//    mlplan.buildClassifier(split.get(0));
//    Evaluation eval = new Evaluation(split.get(0));
//    eval.evaluateModel(mlplan, split.get(1));
//    System.out.println("Error Rate: " + (100 - eval.pctCorrect()) / 100f);
//
//    while (true) {
//      ;
//    }
//  }

}
