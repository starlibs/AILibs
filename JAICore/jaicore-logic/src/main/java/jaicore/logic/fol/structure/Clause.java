package jaicore.logic.fol.structure;

import java.util.Map;

import jaicore.logic.fol.util.LogicUtil;

public class Clause extends LiteralSet {
	public static Clause getByNegatingMonom(final Monom m) {
		Clause c = new Clause();
		for (Literal l : m) {
			c.add(new Literal(l.getProperty(), l.getParameters(), !l.isPositive()));
		}
		return c;
	}

	public Clause() {
		super();
	}

	public Clause(final Literal l) {
		super(l);
	}

	public Clause(final LiteralSet literals, final Map<VariableParam, ? extends LiteralParam> m) {
		super();
		for (Literal l : literals) {
			Literal lCopy = l.clone(m);
			if (lCopy.getPropertyName().equals("=") && lCopy.isGround()) {
				if (LogicUtil.evalEquality(lCopy)) {
					this.clear();
					this.add(new Literal("A"));
					this.add(new Literal("!A"));
					return;
				}
			} else {
				this.add(lCopy);
			}
		}
	}

	public Clause(final String literals) {
		super(literals, "\\|");
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 3915423297171319761L;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		boolean firstElement = true;
		for (Literal l : this) {
			if (firstElement) {
				firstElement = false;
			} else {
				sb.append("|");
			}
			sb.append(l);
		}
		sb.append("]");
		return sb.toString();
	}

	public boolean isTautological() {
		return this.containsPositiveAndNegativeVersionOfLiteral();
	}
}
