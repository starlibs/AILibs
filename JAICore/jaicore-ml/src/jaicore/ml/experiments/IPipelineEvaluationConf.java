package jaicore.ml.experiments;
import java.io.File;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources({ "file:conf/eval.properties" })
public interface IPipelineEvaluationConf extends Config {
	
	  public static final String PREFIX = "pleval.";
	  public static final String PREFIX_SELECTION = PREFIX + "selection.";
	  public static final String PREFIX_MEM = PREFIX + "mem.";
	  public static final String TIMEOUT_TOTAL = PREFIX + "timeout.total";
	  public static final String TIMEOUT_CANDIDATE = PREFIX + "timeout.candidate";
	  public static final String CPUS = PREFIX + "cpus";
	  public static final String MEM_MAX = PREFIX_MEM + "max";
	  public static final String MEM_OPP = PREFIX_MEM + "opp";
	  public static final String RUNS = PREFIX + "runs";
	  public static final String TMPDIR = PREFIX + "tmpdir";
	  public static final String TRAINING = PREFIX + "training";
	  public static final String PHASE2 = PREFIX + "phase2";
	  public static final String SELECTION_ITERATIONS = PREFIX_SELECTION + "iterations";
	  public static final String SELECTION_CANDIDATES = PREFIX_SELECTION + "numberofcandidates";
	  public static final String SOLUTIONLOGDIR = PREFIX + "logdir";
	  
	  public static final String VALIDATION = PREFIX + "validation";

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
	  
	  @Key(TMPDIR)
	  public File getTmpDir();
	  
	  @Key(SOLUTIONLOGDIR)
	  public File getSolutionLogDir();
	  
	  @Key(TRAINING)
	  public int getTrainingPortion();
	  
	  @Key(PHASE2)
	  public int getPortionOfDataForPhase2();
	  
	  @Key(VALIDATION)
	  public String getValidationAlgorithm();
	  
	  @Key(SELECTION_ITERATIONS)
	  public int getNumberOfIterationsInSelectionPhase();
	  
	  @Key(SELECTION_CANDIDATES)
	  public int getNumberOfCandidatesInSelectionPhase();
}
