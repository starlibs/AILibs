package jaicore.logic.fol.structure;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jaicore.logic.fol.util.LogicUtil;

@SuppressWarnings("serial")
public class CNFFormula extends HashSet<Clause> {

	public CNFFormula() {
		super();
	}

	public CNFFormula(final Clause c) {
		super();
		this.add(c);
	}

	public CNFFormula(final Monom m) {
		super();
		for (Literal l : m) {
			this.add(new Clause(l));
		}
	}


	public CNFFormula(final Collection<Clause> c) {
		super();
		this.addAll(c);
	}

	public CNFFormula(final Set<Clause> clauses, final Map<VariableParam, ? extends LiteralParam> mapping) {
		super();
		for (Clause c : clauses) {
			Clause replacedClause = new Clause(c, mapping);

			/* if the clause is empty, it is false */
			if (replacedClause.isEmpty()) {
				this.clear();
				this.add(new Clause("A"));
				this.add(new Clause("!A"));
				return;
			}

			/* if the clause is tautological, we also do not need to add it */
			if (!replacedClause.isTautological()) {
				this.add(replacedClause);
			}
		}
	}

	public Set<VariableParam> getVariableParams() {
		Set<VariableParam> vars = new HashSet<>();
		for (Clause c : this) {
			vars.addAll(c.getVariableParams());
		}
		return vars;
	}

	public Set<ConstantParam> getConstantParams() {
		Set<ConstantParam> constants = new HashSet<>();
		for (Clause c : this) {
			constants.addAll(c.getConstantParams());
		}
		return constants;
	}

	public boolean hasDisjunctions() {
		for (Clause c : this) {
			if (c.size() > 1) {
				return true;
			}
		}
		return false;
	}

	public Monom extractMonom() {
		if (this.hasDisjunctions()) {
			throw new IllegalArgumentException("Cannot extract a monom from a non-monom CNF");
		}
		Monom m = new Monom();
		for (Clause c : this) {
			m.add(c.iterator().next());
		}
		return m;
	}

	public boolean isObviouslyContradictory() {
		return this.contains(new Clause("A")) && this.contains(new Clause("!A"));
	}

	public boolean entailedBy(final Monom m) {
		for (Clause c : this) {
			boolean clauseSatisfied = false;
			for (Literal l : c) {
				if (l.getPropertyName().equals("=")) {
					if (LogicUtil.evalEquality(l)) {
						clauseSatisfied = true;
						break;
					}
				}
				else if (l.isPositive() && m.contains(l) || l.isNegated() && !m.contains(l.clone().toggleNegation())) {
					clauseSatisfied = true;
					break;
				}
			}
			if (!clauseSatisfied) {
				return false;
			}
		}
		return true;
	}
}
