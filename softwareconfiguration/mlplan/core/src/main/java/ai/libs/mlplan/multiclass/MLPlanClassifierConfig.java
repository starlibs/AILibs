package ai.libs.mlplan.multiclass;

import java.util.List;

import org.aeonbits.owner.Config.Sources;

import ai.libs.hasco.twophase.TwoPhaseHASCOConfig;

@Sources({ "file:conf/mlplan.properties" })
public interface MLPlanClassifierConfig extends TwoPhaseHASCOConfig {

	public static final String PREFERRED_COMPONENTS = "mlplan.preferredComponents";
	public static final String SELECTION_PORTION = "mlplan.selectionportion";
	public static final String PRECAUTION_OFFSET = "mlplan.precautionoffset";

	@Key(SELECTION_PORTION)
	@DefaultValue("0.3")
	public double dataPortionForSelection();

	@Key(PRECAUTION_OFFSET)
	@DefaultValue("5")
	public int precautionOffset();

	@Key(PREFERRED_COMPONENTS)
	public List<String> preferredComponents();
}
