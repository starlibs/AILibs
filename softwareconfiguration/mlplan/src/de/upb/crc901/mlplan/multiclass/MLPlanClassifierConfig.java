package de.upb.crc901.mlplan.multiclass;

import java.io.File;

import org.aeonbits.owner.Config.Sources;

import hasco.variants.forwarddecomposition.twophase.TwoPhaseHASCOConfig;

@Sources({ "file:conf/automl/mlplan.properties" })
public interface MLPlanClassifierConfig extends TwoPhaseHASCOConfig {

	public static final String PREFERRED_COMPONENTS = "mlplan.preferredComponents";
	public static final String SEARCH_MCCV_ITERATIONS = "mlplan.search.mccvFolds";
	public static final String SEARCH_MCCV_FOLDSIZE = "mlplan.search.foldSize";
	public static final String SELECTION_MCCV_ITERATIONS = "mlplan.selection.mccvFolds";
	public static final String SELECTION_MCCV_FOLDSIZE = "mlplan.selection.foldSize";
	public static final String SELECTION_PORTION = "mlplan.selection.mccvPortion";
	
//	public static final String TIMEOUT_PER_EVAL_IN_SECONDS = "mlplan.timeoutPerEval";

	@Key(SEARCH_MCCV_ITERATIONS)
	@DefaultValue("5")
	public int numberOfMCIterationsDuringSearch();

	@Key(SEARCH_MCCV_FOLDSIZE)
	@DefaultValue(".7")
	public float getMCCVTrainFoldSizeDuringSearch();

	@Key(SELECTION_MCCV_ITERATIONS)
	@DefaultValue("5")
	public int numberOfMCIterationsDuringSelection();

	@Key(SELECTION_MCCV_FOLDSIZE)
	@DefaultValue(".7")
	public float getMCCVTrainFoldSizeDuringSelection();

	@Key(SELECTION_PORTION)
	@DefaultValue("0.3")
	public float dataPortionForSelection();

//	@Key(TIMEOUT_PER_EVAL_IN_SECONDS)
//	@DefaultValue("10")
//	public int timeoutPerNodeFComputation();

	@Key(PREFERRED_COMPONENTS)
	@DefaultValue("conf/mlplan/precedenceList.txt")
	public File preferredComponents();
}
