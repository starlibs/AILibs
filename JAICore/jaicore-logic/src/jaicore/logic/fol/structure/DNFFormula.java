package jaicore.logic.fol.structure;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jaicore.logic.fol.util.LogicUtil;

@SuppressWarnings("serial")
public class DNFFormula extends HashSet<Monom> {
	public DNFFormula() {
		super();
	}

	public DNFFormula(Monom m) {
		super();
		this.add(m);
	}
	
	public DNFFormula(Clause m) {
		super();
		for (Literal l : m) {
			this.add(new Monom(l));
		}
	}
	
	public DNFFormula(Collection<Monom> m) {
		super();
		this.addAll(m);
	}

	public DNFFormula(Set<Monom> monoms, Map<VariableParam, ? extends LiteralParam> mapping) {
		super();
		for (Monom c : monoms) {
			Monom replacedMonom = new Monom(c, mapping);
			
			/* if the monom is empty, it is false */
			if (replacedMonom.isEmpty()) {
				this.clear();
				this.add(new Monom("A"));
				this.add(new Monom("!A"));
				return;
			}
			
			/* if the monom is contradictory, we also do not need to add it */
			if (!replacedMonom.isContradictory())
				this.add(replacedMonom);
		}
	}

	public Set<VariableParam> getVariableParams() {
		Set<VariableParam> vars = new HashSet<>();
		for (Monom m : this)
			vars.addAll(m.getVariableParams());
		return vars;
	}

	public Set<ConstantParam> getConstantParams() {
		Set<ConstantParam> constants = new HashSet<>();
		for (Monom m : this)
			constants.addAll(m.getConstantParams());
		return constants;
	}

	public boolean hasConjunctions() {
		for (Monom m : this) {
			if (m.size() > 1)
				return true;
		}
		return false;
	}
	
	public Clause extractClause() {
		if (hasConjunctions())
			throw new IllegalArgumentException("Cannot extract a clause from a non-monom DNF");
		Clause c = new Clause();
		for (Monom m : this) {
			c.add(m.iterator().next());
		}
		return c;
	}
	
	public boolean entailedBy(Monom m) {
		for (Monom m2 : this) {
			boolean monomSatisfied = true;
			for (Literal l : m2) {
				if (l.getPropertyName().equals("=")) {
					if (!LogicUtil.evalEquality(l)) {
						monomSatisfied = false;
						break;
					}
					else
						continue;
				}
				else if (!(l.isPositive() && m.contains(l) || l.isNegated() && !m.contains(l.clone().toggleNegation()))) {
					monomSatisfied = false;
					break;
				}
			}
			if (monomSatisfied)
				return true;
		}
		return false;
	}
}
