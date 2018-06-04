package jaicore.logic.fol.structure;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class HornFormula extends HashSet<HornRule> {

	public Set<ConstantParam> getConstantParams() {
		Set<ConstantParam> constants = new HashSet<>();
		for (HornRule r : this) {
			constants.addAll(r.getPremise().getConstantParams());
			constants.addAll(r.getConclusion().getConstantParams());
		}
		return constants;
	}
}
