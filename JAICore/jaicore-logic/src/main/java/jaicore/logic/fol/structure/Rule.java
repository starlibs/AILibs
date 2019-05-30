package jaicore.logic.fol.structure;

import java.util.HashSet;
import java.util.Set;

/**
 * @author wever
 *
 */
public class Rule {

	private final CNFFormula premise;
	private final CNFFormula conclusion;

	public Rule(final CNFFormula pPremise, final CNFFormula pConclusion) {
		this.premise = pPremise;
		this.conclusion = pConclusion;
	}

	public CNFFormula getPremise() {
		return this.premise;
	}

	public CNFFormula getConclusion() {
		return this.conclusion;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(this.premise);
		sb.append(" => ");
		sb.append(this.conclusion);

		return sb.toString();
	}

	public Set<ConstantParam> getConstantParams() {
		Set<ConstantParam> constants = new HashSet<>();
		constants.addAll(this.premise.getConstantParams());
		constants.addAll(this.conclusion.getConstantParams());
		return constants;
	}
}
