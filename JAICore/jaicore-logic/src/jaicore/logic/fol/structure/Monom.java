package jaicore.logic.fol.structure;

import java.util.Collection;
import java.util.Map;

import jaicore.basic.sets.SetUtil;

public class Monom extends LiteralSet {

	public static Monom fromCNFFormula(CNFFormula formula) {
		Monom m = new Monom();
		for (Clause c : formula) {
			if (c.size() > 1) {
				throw new IllegalArgumentException("Monom constructor says: Cannot create monom from CNF with disjunctions " + formula);
			}
			m.addAll(c);
		}
		return m;
	}

	public Monom() {
		super();
	}
	
	public Monom(Literal l) {
		super(l);
	}
	
	public Monom(String literals) {
		super(literals, "&");
	}

	public Monom(Collection<Literal> set) {
		this(set, true);
	}
	
	public Monom(Collection<Literal> set, boolean deep) {
		super(set, deep);
	}

	public Monom(Collection<Literal> literals, Map<? extends LiteralParam, ? extends LiteralParam> mapping) {
		super(literals, mapping);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1279300062766067057L;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean firstElement = true;
		for (Literal l : this) {
			if (firstElement) {
				firstElement = false;
			} else {
				sb.append("&");
			}
			sb.append(l);
		}

		return sb.toString();
	}

	public boolean isContradictory() {
		return this.containsPositiveAndNegativeVersionOfLiteral() || containsGroundEqualityPredicateThatEvaluatesTo(false);
	}

	public boolean isConsistent() {
		return !isContradictory();
	}
	
	/**
	 * @param conclusion
	 *            Another literal set that may be concluded by this literal set.
	 * @return True, if this literal set logically implies the conclusion literal set under any partial mapping.
	 */
	public boolean implies(LiteralSet conclusion) throws InterruptedException {
		// check all partial mappings for implication
		for (Map<VariableParam, VariableParam> mapping : SetUtil.allMappings(this.getVariableParams(), conclusion.getVariableParams(), false, false, false)) {
			if (new LiteralSet(this, mapping).containsAll(conclusion))
				return true; // implication mapping found
		}

		return false; // no implying mapping
	}

	public Map<VariableParam, VariableParam> getImplyingMappingThatMapsFromConclusionVarsToPremiseVars(LiteralSet conclusion) throws InterruptedException {
		for (Map<VariableParam, VariableParam> mapping : SetUtil.allMappings(conclusion.getVariableParams(), this.getVariableParams(), false, false, false))
			if (this.containsAll(new LiteralSet(conclusion, mapping)))
				return mapping; // implication mapping found

		return null; // no implying mapping
	}

	public CNFFormula asCNF() {
		CNFFormula formula = new CNFFormula();
		for (Literal l : this)
			formula.add(new Clause(l));
		return formula;
	}	
}
