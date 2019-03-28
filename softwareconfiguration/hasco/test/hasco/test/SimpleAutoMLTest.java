//package hasco.test;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//
//import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
//
//import hasco.RefinementConfiguredSoftwareConfigurationProblem;
//import hasco.SoftwareConfigurationProblem;
//import hasco.core.HASCOFD;
//import hasco.core.HASCOSolutionCandidate;
//import hasco.model.Component;
//import hasco.model.NumericParameterDomain;
//import hasco.model.Parameter;
//import hasco.model.ParameterRefinementConfiguration;
//import hasco.variants.HASCOViaFD;
//import hasco.variants.HASCOViaFDAndBestFirstWithRandomCompletions;
//import jaicore.graphvisualizer.gui.VisualizationWindow;
//import jaicore.ml.WekaUtil;
//import jaicore.planning.EvaluatedSearchGraphBasedPlan;
//import jaicore.planning.graphgenerators.task.tfd.TFDNode;
//import jaicore.planning.graphgenerators.task.tfd.TFDTooltipGenerator;
//import jaicore.search.model.travesaltree.Node;
//import weka.classifiers.AbstractClassifier;
//import weka.classifiers.Classifier;
//import weka.classifiers.Evaluation;
//import weka.core.Instances;
//
//public class SimpleAutoMLTest {
//
////  @Test
//  public void test() throws Exception {
//
//    /* read instances */
//    Instances data = new Instances(new BufferedReader(new FileReader(new File("../datasets/classification/multi-class/segment.arff"))));
//    data.setClassIndex(data.numAttributes() - 1);
//
//    /* create algorithm */
//    List<Component> components = new ArrayList<>();
//    Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramConfigs = new HashMap<>();
//
//    {
//    Component c = null;
//    Parameter p;
//    Map<Parameter, ParameterRefinementConfiguration> paramConfig;
//
//    // c = new Component("weka.classifiers.meta.AdaBoostM1");
//    // c.addProvidedInterface("classifier");
//    // c.addRequiredInterface("baseclassifier");
//    // p = new NumericParameter("p1", true, 0, -10, 10);
//    // paramConfig = new HashMap<>();
//    // paramConfig.put(p, new ParameterRefinementConfiguration(2, 1));
//    // c.addParameter(p);
//    // p = new NumericParameter("p2", false, 0, -10, 10);
//    // paramConfig.put(p, new ParameterRefinementConfiguration(2, 1));
//    // c.addParameter(p);
//    // paramConfigs.put(c, paramConfig);
//    // hasco.addComponent(c);
//
//    // c = new Component("weka.classifiers.trees.RandomForest");
//    // c.addProvidedInterface("classifier");
//    // c.addProvidedInterface("baseclassifier");
//    // p = new NumericParameter("I", true, 50, 1, 1000);
//    // paramConfig = new HashMap<>();
//    // paramConfig.put(p, new ParameterRefinementConfiguration(4, 5));
//    // c.addParameter(p);
//    // p = new NumericParameter("P", true, 0, 0, 100);
//    // paramConfig.put(p, new ParameterRefinementConfiguration(2, .05));
//    // c.addParameter(p);
//    // paramConfigs.put(c, paramConfig);
//    // hasco.addComponent(c);
//
//    c = new Component("weka.classifiers.trees.RandomTree");
//    c.addProvidedInterface("classifier");
//    c.addProvidedInterface("baseclassifier");
//    p = new Parameter("M", new NumericParameterDomain(true, 1, 10), 1);
//    paramConfig = new HashMap<>();
//    paramConfig.put(p, new ParameterRefinementConfiguration(8, 1));
//    c.addParameter(p);
//    p = new Parameter("K", new NumericParameterDomain(true, 0, 10), 0);
//    paramConfig.put(p, new ParameterRefinementConfiguration(2, 1));
//    c.addParameter(p);
//    paramConfigs.put(c, paramConfig);
//    components.add(c);
//    }
//    
//    components, paramConfigs, groundComponent -> {
//        Component component = groundComponent.getComponent();
//        Map<String, String> paramValues = groundComponent.getParameterValues();
//        String className = component.getName();
//        try {
//          List<String> params = new ArrayList<>();
//          for (Parameter p : component.getParameters()) {
//            if (paramValues.containsKey(p.getName())) {
//              params.add("-" + p.getName());
//              params.add(paramValues.get(p.getName()));
//            }
//          }
//          String[] paramsAsArray = params.toArray(new String[] {});
//          return AbstractClassifier.forName(className, paramsAsArray);
//        } catch (Exception e) {
//          e.printStackTrace();
//          return null;
//        }
//      }, "classifier", c -> {
//
//        System.out.print("Evaluating solution ... ");
//        DescriptiveStatistics stats = new DescriptiveStatistics();
//        for (int i = 0; i < 2; i++) {
//          List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(i), .7f);
//          c.buildClassifier(split.get(0));
//          Evaluation eval = new Evaluation(split.get(0));
//          eval.evaluateModel(c, split.get(1));
//          stats.addValue((100 - eval.pctCorrect()) / 100);
//        }
//        System.out.println("done");
//        return stats.getMean();
//      }
//    SoftwareConfigurationProblem<Double> coreProblem = new SoftwareConfigurationProblem<>(components, requiredInterface, compositionEvaluator)
//    RefinementConfiguredSoftwareConfigurationProblem<Double> problem = new RefinementConfiguredSoftwareConfigurationProblem<>(coreProblem, paramRefinementConfig); 
//    
//    HASCOViaFDAndBestFirstWithRandomCompletions<Double> hasco = new HASCOViaFDAndBestFirstWithRandomCompletions<>();
//
//    new VisualizationWindow<Node<TFDNode, String>>(hasco).setTooltipGenerator(new TFDTooltipGenerator<>());
//    for (HASCOSolutionCandidate<EvaluatedSearchGraphBasedPlan, Classifier, Double> candidate : hasco) {
//      System.out.println(candidate);
//    }
//    System.out.println("Ready");
//    while (true) {
//      ;
//    }
//  }
//}
