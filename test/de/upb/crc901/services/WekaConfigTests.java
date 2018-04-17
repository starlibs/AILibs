package de.upb.crc901.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.upb.crc901.automl.pipeline.service.MLPipelinePlan;
import de.upb.crc901.automl.pipeline.service.MLServicePipeline;
import de.upb.crc901.services.core.HttpServiceClient;
import de.upb.crc901.services.core.HttpServiceServer;
import de.upb.crc901.services.core.OntologicalTypeMarshallingSystem;
import de.upb.crc901.services.core.ServiceCompositionResult;
import de.upb.crc901.services.core.ServiceHandle;
import jaicore.ml.WekaUtil;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * Tests the configuration of jase server regarding weka classifiers and preprocessors.
 *
 * @author aminfaez
 *
 */
public class WekaConfigTests {
  private final static int PORT = 8000;

  private static HttpServiceServer server;

  private static HttpServiceClient client;
  private static final OntologicalTypeMarshallingSystem otms = new OntologicalTypeMarshallingSystem();

  private static Instances wekaInstances;

  // names of base classes in the configuration
  private static String baseClassifierConfigName = "$base_weka_classifier_config$";
  private static String basePreprocessorConfigName = "$base_weka_filter_config$";

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    /* start server */
    server = HttpServiceServer.TEST_SERVER();

    client = new HttpServiceClient(otms);
    wekaInstances = new Instances(new BufferedReader(new FileReader("../CrcTaskBasedConfigurator/testrsc" + File.separator + "polychotomous" + File.separator + "audiology.arff")));

    wekaInstances.setClassIndex(wekaInstances.numAttributes() - 1);

    // print all classifiers and preprocessors that are enumerated in the classes configuration.
    System.out.println("WEKA CLASSFIERS:");
    for (String wekaClassifier : server.getClassesConfig().allSubconfigs(baseClassifierConfigName)) {
      System.out.println("\t" + wekaClassifier);
    }
    System.out.println("WEKA FILTERS:");
    for (String wekaPreprocessor : server.getClassesConfig().allSubconfigs(basePreprocessorConfigName)) {
      System.out.println("\t" + wekaPreprocessor);
    }
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    server.shutdown();
  }

  @Test
  /**
   * Iterates over all classifiers in the config and tries to train and evaluate them.
   */
  public void testAllClassifiers() throws IOException {
    // prepare the data
    List<Instances> split = WekaUtil.getStratifiedSplit(wekaInstances, new Random(0), .9f);
    Set<String> errorSet = new HashSet<>();
    for (String wekaClassifierClasspath : server.getClassesConfig().allSubconfigs(baseClassifierConfigName)) {
      try { 
    	  	MLPipelinePlan plan = new MLPipelinePlan().onHost("localhost", PORT);
    	  	plan.setClassifier(wekaClassifierClasspath);
        MLServicePipeline pl = new MLServicePipeline(plan);

        pl.buildClassifier(split.get(0));

        int mistakes = 0;
        int index = 0;
        double[] predictions = pl.classifyInstances(split.get(1));
        for (Instance instance : split.get(1)) {
          double prediction = predictions[index];
          if (instance.classValue() != prediction) {
            mistakes++;
          }
          index++;
        }
        System.out.println("Accuracy of " + wekaClassifierClasspath + ": " + (mistakes * 1f / split.get(1).size()));

      } catch (ClassCastException ex) {
        ex.printStackTrace();
        errorSet.add(wekaClassifierClasspath);
      } catch (Exception ex) {
        // ex.printStackTrace();
        errorSet.add(wekaClassifierClasspath);
      }
    }
    String errorSetPrettyPrint = errorSet.stream().collect(Collectors.joining("\n\t"));
    System.out.println("Error occurred with these classifiers:\n\t" + errorSetPrettyPrint);
  }

  // @Test
  public void testAllPreprocessors() throws IOException {
    Set<String> errorSet = new HashSet<>();
    for (String wekaPreprocessor : server.getClassesConfig().allSubconfigs(basePreprocessorConfigName)) {
      try {
        // Create preprocessor service
        ServiceHandle service = (ServiceHandle) client.callServiceOperation("localhost:" + PORT + "/" + wekaPreprocessor + "::__construct").get("out").getData();
        Assert.assertNotNull(service.getServiceAddress());
        // preprocess data
        ServiceCompositionResult result = client.callServiceOperation(service.getServiceAddress() + "::preprocess", wekaInstances);
        if (!result.containsKey("out") || result.get("out") == null) {
          errorSet.add(wekaPreprocessor);
        }
      } catch (Exception ex) {
        ex.printStackTrace();
        errorSet.add(wekaPreprocessor);
      }
    }
    System.out.println("Error occurred with these preprocessors:\n" + errorSet);
  }
}
