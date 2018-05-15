package de.upb.crc901.mlplan.nips2018;

import de.upb.crc901.automl.hascoscikitlearnml.HASCOForScikitLearnML;
import de.upb.crc901.automl.hascoscikitlearnml.HASCOForScikitLearnML.HASCOForScikitLearnMLSolution;

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

import weka.core.Instances;

public class NIPS2018 {
  @Test
  public void MLPlanTest() throws Exception {

    /* read data and split */
    Instances data = new Instances(new BufferedReader(new FileReader(new File("../datasets/classification/multi-class/audiology.arff"))));
    data.setClassIndex(data.numAttributes() - 1);
    List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(0), .7f);

    HASCOForScikitLearnML hasco = new HASCOForScikitLearnML();
    // mlplan.registerListener(new HASCOSQLEventLogger<>(new MySQLAdapter("isys-db.cs.upb.de", "mlplan",
    // "UMJXI4WlNqbS968X", "hasco")));
    // mlplan.registerListener(new HASCOSQLEventLogger<>(new MySQLAdapter("hsqldb:hsql", "localhost",
    // "SA", "", "testdb", new Properties())));

    new SimpleGraphVisualizationWindow<Node<TFDNode, Double>>(hasco).getPanel().setTooltipGenerator(new TFDTooltipGenerator<>());
    // hasco.setPreferredNodeEvaluator(n -> n.externalPath().size() * 1.0);

    hasco.setNumberOfCPUs(8);
    hasco.gatherSolutions(split.get(0), 60 * 1000 * 5);
    HASCOForScikitLearnMLSolution bestSolution = hasco.getCurrentlyBestSolution();
    // System.out.println("Best solution: " + bestSolution.getClassifier().getClass().getName() +
    // Arrays.toString(bestSolution.getClassifier().getOptions()) + " with base learner "
    // + ((SingleClassifierEnhancer) bestSolution.getClassifier()).getClassifier().getClass().getName()
    // + ". Score: " + bestSolution.getScore());

    while (true) {
      ;
    }
  }
}
