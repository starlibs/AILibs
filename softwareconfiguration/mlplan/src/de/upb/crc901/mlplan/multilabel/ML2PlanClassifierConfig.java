package de.upb.crc901.mlplan.multilabel;

import org.aeonbits.owner.Config.Sources;

import de.upb.crc901.mlplan.multilabel.mekamlplan.ML2PlanMekaClassifier;
import hasco.variants.forwarddecomposition.twophase.TwoPhaseHASCOConfig;

/**
 * Configuration for the {@link ML2PlanMekaClassifier}.
 * 
 * @author Helena Graf
 *
 */
@Sources({ "file:conf/automl/ml2plan.properties" })
public interface ML2PlanClassifierConfig extends TwoPhaseHASCOConfig {

	public static final String SEARCH_MCCV_ITERATIONS = "mlplan.search.mccvFolds";
	public static final String SEARCH_MCCV_FOLDSIZE = "mlplan.search.foldSize";
	public static final String SELECTION_MCCV_ITERATIONS = "mlplan.selection.mccvFolds";
	public static final String SELECTION_MCCV_FOLDSIZE = "mlplan.selection.foldSize";
	public static final String SELECTION_PORTION = "mlplan.selection.mccvPortion";

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
	@DefaultValue("0.0")
	public float dataPortionForSelection();
}
