package de.upb.crc901.mlplan.nips2018;

import de.upb.crc901.automl.hascoscikitlearnml.HASCOForScikitLearnML;
import de.upb.crc901.automl.hascoscikitlearnml.HASCOForScikitLearnML.HASCOForScikitLearnMLSolution;
import de.upb.crc901.automl.hascoscikitlearnml.HASCOForScikitLearnMLConfig;
import de.upb.crc901.automl.hascoscikitlearnml.ScikitLearnBenchmark;

import jaicore.basic.MySQLAdapter;
import jaicore.basic.chunks.Task;
import jaicore.basic.chunks.TaskChunk;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.ml.WekaUtil;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.graphgenerators.task.tfd.TFDTooltipGenerator;
import jaicore.search.structure.core.Node;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.aeonbits.owner.ConfigCache;
import org.codehaus.plexus.util.FileUtils;

import hasco.eventlogger.HASCOSQLEventLogger;
import weka.core.Instances;

public class NIPS2018 {
  private static final String SQL_RUN_RETRIEVAL = "SELECT * FROM experiments WHERE timeout=? AND splitTechnique=?";
  private static final String SQL_REGISTER_RUN = "INSERT INTO experiments (dataset, seed, timeout, splitTechnique) VALUES (?,?,?,?);";
  private static final String SQL_INSERT_RESULT = "INSERT INTO experimentresult (run_id,pipeline,testErrorRate, validationErrorRate) VALUES (?,?,?,?);";

  private static void log(final String msg) {
    System.out.println("[" + Thread.currentThread().getName() + "] " + new Time(System.currentTimeMillis()) + ": " + msg);
  }

  public static void main(final String[] args) throws Exception {
    if (args.length < 1 || args.length > 5) {
      System.out.println("Correct Usage: [tmpFolder] <timeout> <evaluationTimeout> <datasetFolder> <samples>");
      System.exit(0);
    }

    String tmpFolder = args[0];
    HASCOForScikitLearnMLConfig config = ConfigCache.getOrCreate(HASCOForScikitLearnMLConfig.class);
    config.setProperty(HASCOForScikitLearnMLConfig.K_TMP_FOLDER, tmpFolder);
    log("Set tmp folder to" + tmpFolder);

    if (args.length > 1) {
      int timeout = Integer.parseInt(args[1]);
      config.setProperty(HASCOForScikitLearnMLConfig.K_TIMEOUT, timeout + "");
      log("Set global timeout to " + timeout);
    }

    if (args.length > 2) {
      int evalTimeout = Integer.parseInt(args[2]);
      config.setProperty(HASCOForScikitLearnMLConfig.K_EVAL_TIMEOUT, evalTimeout + "");
      log("Set evaluation timeout to " + evalTimeout);
    }
    if (args.length > 3) {
      String datasetFolder = args[3];
      config.setProperty(HASCOForScikitLearnMLConfig.K_DATASET_FOLDER, datasetFolder);
      log("Set dataset folder to " + datasetFolder);
    }

    if (args.length > 4) {
      int samples = Integer.parseInt(args[4]);
      config.setProperty(HASCOForScikitLearnMLConfig.K_NUM_SAMPLES, samples + "");
      log("Set number of samples to " + samples);
    }

    log("Config Object: " + config.toString());

    Task run = getRun(config);
    if (run == null) {
      log("No more task to work on. So shut down.");
      System.exit(0);
    }

    config.setProperty(HASCOForScikitLearnMLConfig.K_RUN_ID, run.getValueAsString("run_id"));

    MySQLAdapter mysql = new MySQLAdapter(config.getDBHost(), config.getDBUser(), config.getDBPassword(), config.getDBDatabase());

    log("Perform experiment " + run);

    /* Prepare the temporary data folder */
    log("Clean up " + config.getTmpFolder());
    if (!config.getTmpFolder().exists()) {
      config.getTmpFolder().mkdirs();
    }
    if (config.getTmpFolder() != null && config.getTmpFolder().listFiles().length > 0) {
      for (File f : config.getTmpFolder().listFiles()) {
        f.delete();
      }
    }
    FileUtils.copyDirectoryStructure(config.getTemplateFolder(), config.getTmpFolder());

    /* Load dataset and create the respective files for evaluation */
    File datasetFile = new File(config.getDatasetFolder().getAbsolutePath() + File.separator + run.getValueAsString("dataset"));
    log("Load dataset File: " + datasetFile.getCanonicalPath());
    Instances data = new Instances(new BufferedReader(new FileReader(datasetFile)));
    data.setClassIndex(data.numAttributes() - 1);

    List<Instances> trainTestSplit = WekaUtil.getStratifiedSplit(data, new Random(run.getValueAsInt("seed")), .7f);
    List<Instances> searchSelectSplit = WekaUtil.getStratifiedSplit(trainTestSplit.get(0), new Random(run.getValueAsInt("seed")), .7f);
    Instances selectionData = trainTestSplit.get(0);
    Instances searchData = searchSelectSplit.get(0);

    createTrainTestData(config, trainTestSplit, "all_");
    ScikitLearnBenchmark testBenchmark = new ScikitLearnBenchmark(trainTestSplit, "all", mysql, run);
    ScikitLearnBenchmark selectionBenchmark = new ScikitLearnBenchmark(selectionData, 10, .7, -1, 10, "select_", mysql, run);
    ScikitLearnBenchmark searchBenchmark = new ScikitLearnBenchmark(searchData, 5, .7, config.getEvaluationTimeout() * 1000, 10, "val_", mysql, run);

    /* Set Up HASCO instance */
    HASCOForScikitLearnML hasco = new HASCOForScikitLearnML();
    if (config.getRegisterMySQLListener()) {
      hasco.registerListener(new HASCOSQLEventLogger<>(new MySQLAdapter(config.getDBHost(), config.getDBUser(), config.getDBPassword(), config.getDBDatabase())));
    }
    if (config.getShowGraphVisualization()) {
      new SimpleGraphVisualizationWindow<Node<TFDNode, Double>>(hasco).getPanel().setTooltipGenerator(new TFDTooltipGenerator<>());
      hasco.setPreferredNodeEvaluator(n -> n.externalPath().size() * 1.0);
    }
    hasco.setNumberOfCPUs(config.getCPUs());

    /* start gathering solutions */
    config.setProperty(HASCOForScikitLearnMLConfig.K_RUN_START_TIMESTAMP, System.currentTimeMillis() + ""); // log timestamp of execution start
    hasco.gatherSolutions(searchBenchmark, selectionBenchmark, testBenchmark, 1000 * run.getValueAsInt("timeout"), mysql);

    HASCOForScikitLearnMLSolution bestSolution = hasco.getCurrentlyBestSolution();

    log("==================");
    hasco.cancel();
    log("Best solution selected for dataset " + run.getValueAsString("dataset") + " testError: " + bestSolution.getTestScore() + " selectionError"
        + bestSolution.getSelectionScore() + " validationError: " + bestSolution.getValidationScore());

    Map<String, String> conditions = new HashMap<>();
    conditions.put("run_id", run.getValueAsString("run_id"));
    Map<String, String> updateValues = new HashMap<>();
    updateValues.put("finished", "1");

    try {
      mysql.update("experiments", updateValues, conditions);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    System.exit(0);
  }

  private static void createTrainTestData(final HASCOForScikitLearnMLConfig config, final List<Instances> selectionSplit, final String prefix) throws IOException {
    File trainFile = new File(config.getTmpFolder().getAbsolutePath() + File.separator + prefix + "train.arff");
    File testFile = new File(config.getTmpFolder().getAbsolutePath() + File.separator + prefix + "test.arff");

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(trainFile))) {
      bw.write(selectionSplit.get(0).toString());
    }
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(testFile))) {
      bw.write(selectionSplit.get(1).toString());
    }
  }

  private static Task getRun(final HASCOForScikitLearnMLConfig config) throws SQLException {
    TaskChunk<Task> chunk = new TaskChunk<>("chunkID=registeredRuns");
    try (Connection con = DriverManager.getConnection(
        "jdbc:mysql://" + config.getDBHost() + "/" + config.getDBDatabase() + "?autoReconnect=true&verifyServerCertificate=false&requireSSL=true&useSSL=true", config.getDBUser(),
        config.getDBPassword())) {
      PreparedStatement prep = con.prepareStatement(SQL_RUN_RETRIEVAL);
      prep.setInt(1, config.getTimeout());
      prep.setString(2, config.getSplitTechnique());
      ResultSet res = prep.executeQuery();
      while (res.next()) {
        Task t = new Task();
        t.store("run_id", res.getInt("run_id"));
        t.store("dataset", res.getString("dataset"));
        t.store("seed", res.getInt("seed"));
        t.store("timeout", res.getInt("timeout"));
        t.store("splitTechnique", res.getString("splitTechnique"));
        t.setChunk(chunk);
        chunk.add(t);
      }

      TaskChunk<Task> allRuns = new TaskChunk<>("chunkId=allRuns");
      for (File dataset : config.getDatasetFolder().listFiles()) {
        for (int seed = 0; seed < config.getSamples(); seed++) {
          Task t = new Task();
          t.store("dataset", dataset.getName());
          t.store("seed", seed);
          t.store("timeout", config.getTimeout());
          t.store("splitTechnique", config.getSplitTechnique());
          t.setChunk(allRuns);
          allRuns.add(t);
        }
      }
      int numAllRuns = allRuns.size();

      for (Task t : chunk) {
        Map<String, String> selection = new HashMap<>();
        String[] keysToSelect = { "dataset", "seed", "timeout", "splitTechnique" };
        Arrays.stream(keysToSelect).forEach(x -> selection.put(x, t.getValueAsString(x)));
        TaskChunk<Task> selected = allRuns.select(selection);
        for (Task toRemove : selected) {
          allRuns.remove(toRemove);
        }
      }
      System.out.println("Number of all runs: " + numAllRuns);
      System.out.println("Number of already registered runs: " + chunk.size());
      System.out.println("Number of remaining runs: " + allRuns.size());
      System.out.println("Already finished runs: " + (numAllRuns - allRuns.size()));

      while (allRuns.size() > 0) {
        int taskIndex = new Random().nextInt(allRuns.size());
        Task reg = allRuns.get(taskIndex);
        PreparedStatement prepStat = con.prepareStatement(SQL_REGISTER_RUN, 1);
        prepStat.setString(1, reg.getValueAsString("dataset"));
        prepStat.setInt(2, reg.getValueAsInt("seed"));
        prepStat.setInt(3, reg.getValueAsInt("timeout"));
        prepStat.setString(4, reg.getValueAsString("splitTechnique"));

        try {
          prepStat.execute();
          ResultSet resReg = prepStat.getGeneratedKeys();
          resReg.next();
          reg.store("run_id", resReg.getInt(1));
          return reg;
        } catch (Exception e) {
          e.printStackTrace();
          allRuns.remove(taskIndex);
        }
      }
    }

    return null;
  }

}
