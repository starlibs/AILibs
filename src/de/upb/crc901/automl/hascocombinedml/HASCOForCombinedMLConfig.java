package de.upb.crc901.automl.hascocombinedml;

import java.io.File;

import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Mutable;

@Sources({ "file:HASCOForComboML.properties" })
public interface HASCOForCombinedMLConfig extends Mutable {

  public static final String K_TMP_FOLDER = "hasco.sklearn.tmp";
  public static final String K_TEMPLATE_FOLDER = "hasco.sklearn.template";
  public static final String K_CANDIDATE_NAME = "hasco.sklearn.candidatename";

  public static final String K_NUM_CPUS = "hasco.run.cpus";
  public static final String K_DATASET_FOLDER = "hasco.run.datasetfolder";
  public static final String K_NUM_SAMPLES = "hasco.run.samples";
  public static final String K_TIMEOUT = "hasco.run.timeout";
  public static final String K_EVAL_TIMEOUT = "hasco.db.evaluationtimeout";
  public static final String K_SPLIT_TECHNIQUE = "hasco.run.splittechnique";
  public static final String K_REQUEST_INTERFACE = "hasco.run.requestinterface";

  public static final String K_DB_HOST = "hasco.db.host";
  public static final String K_DB_USER = "hasco.db.user";
  public static final String K_DB_PASSWORD = "hasco.db.password";
  public static final String K_DB_DATABASE = "hasco.db.database";

  public static final String K_REG_MYSQL_LISTENER = "hasco.run.mysqlreport";
  public static final String K_SHOW_GRAPH_VISUALIZATION = "hasco.run.showgraph";
  public static final String K_COMPONENT_FILE = "hasco.run.componentfile";
  public static final String K_RUN_START_TIMESTAMP = "hasco.run.starttimestamp";
  public static final String K_COMPLEXITY_FACTOR = "hasco.run.complexityfactor";
  public static final String K_RUN_ID = "hasco.run.run_id";
  public static final String K_SEED = "hasco.run.seed";

  @Key(K_TMP_FOLDER)
  @DefaultValue("tmp/")
  public File getTmpFolder();

  @Key(K_CANDIDATE_NAME)
  @DefaultValue("candidate")
  public String getCandidateScriptName();

  @Key(K_TEMPLATE_FOLDER)
  @DefaultValue("testrsc/hascoSL/")
  public File getTemplateFolder();

  @Key(K_NUM_CPUS)
  @DefaultValue("8")
  public int getCPUs();

  @Key(K_DATASET_FOLDER)
  @DefaultValue("../datasets/classification/multi-class")
  public File getDatasetFolder();

  @Key(K_NUM_SAMPLES)
  @DefaultValue("10")
  public int getSamples();

  @Key(K_TIMEOUT)
  @DefaultValue("300")
  public int getTimeout();

  @Key(K_EVAL_TIMEOUT)
  @DefaultValue("30")
  public int getEvaluationTimeout();

  @Key(K_SPLIT_TECHNIQUE)
  @DefaultValue("5-70/30-mccv")
  public String getSplitTechnique();

  @Key(K_DB_HOST)
  public String getDBHost();

  @Key(K_DB_USER)
  public String getDBUser();

  @Key(K_DB_PASSWORD)
  public String getDBPassword();

  @Key(K_DB_DATABASE)
  public String getDBDatabase();

  @Key(K_REG_MYSQL_LISTENER)
  @DefaultValue("false")
  public boolean getRegisterMySQLListener();

  @Key(K_SHOW_GRAPH_VISUALIZATION)
  @DefaultValue("false")
  public boolean getShowGraphVisualization();

  @Key(K_COMPONENT_FILE)
  @DefaultValue("model/scikit-learn/ml-plan-ul.json")
  public File getComponentFile();

  @Key(K_RUN_START_TIMESTAMP)
  public long getRunStartTimestamp();

  @Key(K_COMPLEXITY_FACTOR)
  @DefaultValue("0.0")
  public double getComplexityFactor();

  @Key(K_RUN_ID)
  public int getRunID();

  @Key(K_SEED)
  public long getSeed();

  @Key(K_REQUEST_INTERFACE)
  @DefaultValue("AbstractClassifier")
  public String getRequestedInterface();
}
