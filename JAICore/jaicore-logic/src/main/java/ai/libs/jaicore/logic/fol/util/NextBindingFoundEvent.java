package ai.libs.jaicore.logic.fol.util;

import java.util.Map;

import ai.libs.jaicore.basic.algorithm.events.AAlgorithmEvent;
import ai.libs.jaicore.logic.fol.structure.LiteralParam;
import ai.libs.jaicore.logic.fol.structure.VariableParam;

public class NextBindingFoundEvent extends AAlgorithmEvent {
	private final Map<VariableParam, LiteralParam> grounding;

	public NextBindingFoundEvent(String algorithmId, Map<VariableParam, LiteralParam> grounding) {
		super(algorithmId);
		this.grounding = grounding;
	}

	public Map<VariableParam, LiteralParam> getGrounding() {
		return grounding;
	}
}
