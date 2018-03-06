package de.upb.crc901.mlplan.scc2018;
import java.io.File;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources({ "file:./conf/scc2018.properties" })
public interface IPipelineEvaluationConf extends Config {
	
	  public static final String PREFIX = "pleval.";
	  public static final String DBPREFIX = "database.";
	  
	  public static final String PREFIX_SELECTION = PREFIX + "selection.";
	  public static final String PREFIX_MEM = PREFIX + "mem.";
	  public static final String TIMEOUT_TOTAL = PREFIX + "timeout.total";
	  public static final String TIMEOUT_CANDIDATE = PREFIX + "timeout.candidate";
	  public static final String CPUS = PREFIX + "cpus";
	  public static final String MEM_MAX = PREFIX_MEM + "max";
	  public static final String MEM_OPP = PREFIX_MEM + "opp";
	  public static final String RUNS = PREFIX + "runs";
	  public static final String F_SAMPLES = PREFIX + "fsamples";
	  public static final String TMPDIR = PREFIX + "tmpdir";
	  public static final String TRAINING = PREFIX + "training";
	  public static final String SELECTION_PORTION = PREFIX_SELECTION + "dataportion";
	  public static final String SELECTION_ITERATIONS = PREFIX_SELECTION + "iterations";
	  public static final String SELECTION_CANDIDATES = PREFIX_SELECTION + "numberofcandidates";
	  public static final String SOLUTIONLOGDIR = PREFIX + "logdir";
	  public static final String VALIDATION = PREFIX + "validation";
	  
	  /* database settings */
	  public static final String DB_HOST = DBPREFIX + "host";
	  public static final String DB_USER = DBPREFIX + "username";
	  public static final String DB_PASS = DBPREFIX + "password";
	  public static final String DB_NAME = DBPREFIX + "dbname";
	  

	  // GET
	  @Key(TIMEOUT_TOTAL)
	  public int getTimeoutTotal();
	  
	  @Key(TIMEOUT_CANDIDATE)
	  public int getTimeoutPerCandidate();
	  
	  @Key(CPUS)
	  public int getNumberOfAllowedCPUs();
	  
	  @Key(MEM_MAX)
	  public int getMemoryLimitinMB();
	  
	  @Key(MEM_OPP)
	  public int getAssumedMemoryOverheadPerProcess();
	  
	  @Key(RUNS)
	  public int getNumberOfRuns();
	  
	  @Key(F_SAMPLES)
	  public int getNumberOfSamplesInFValueComputation();
	  
	  @Key(TMPDIR)
	  public File getTmpDir();
	  
	  @Key(SOLUTIONLOGDIR)
	  public File getSolutionLogDir();
	  
	  @Key(TRAINING)
	  public int getTrainingPortion();
	  
	  
	  
	  @Key(VALIDATION)
	  public String getValidationAlgorithm();
	  
	  @Key(SELECTION_PORTION)
	  public float getPortionOfDataForPhase2();
	  
	  @Key(SELECTION_ITERATIONS)
	  public int getNumberOfIterationsInSelectionPhase();
	  
	  @Key(SELECTION_CANDIDATES)
	  public int getNumberOfCandidatesInSelectionPhase();
	  
	  @Key(DB_HOST)
	  public String getDBHost();
	  
	  @Key(DB_USER)
	  public String getDBUsername();
	  
	  @Key(DB_PASS)
	  public String getDBPassword();
	  
	  @Key(DB_NAME)
	  public String getDBDatabaseName();
}
