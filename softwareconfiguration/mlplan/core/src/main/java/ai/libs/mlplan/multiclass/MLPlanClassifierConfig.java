package ai.libs.mlplan.multiclass;

import java.io.File;

import org.aeonbits.owner.Config.Sources;

import ai.libs.hasco.variants.forwarddecomposition.twophase.TwoPhaseHASCOConfig;

@Sources({ "file:conf/mlplan.properties" })
public interface MLPlanClassifierConfig extends TwoPhaseHASCOConfig {

	public static final String PREFERRED_COMPONENTS = "mlplan.preferredComponents";
	public static final String SELECTION_PORTION = "mlplan.selectionportion";

	@Key(SELECTION_PORTION)
	public double dataPortionForSelection();

	@Key(PREFERRED_COMPONENTS)
	public File preferredComponents();
}
