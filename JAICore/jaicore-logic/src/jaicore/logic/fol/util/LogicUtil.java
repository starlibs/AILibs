package jaicore.logic.fol.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import jaicore.basic.sets.SetUtil;
import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.Clause;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.structure.LiteralSet;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.Type;
import jaicore.logic.fol.structure.VariableParam;

/**
 * Utility class for the logic package.
 * 
 * @author fmohr, mbunse
 */
public class LogicUtil {

	/**
	 * @param a
	 *            The literal set A.
	 * @param b
	 *            The literal set B.
	 * @return The intersection of A and B.
	 */
	public static LiteralSet intersectionOfLiteralSets(LiteralSet a, LiteralSet b) {

		return new LiteralSet(SetUtil.intersection(a, b));

	}

	/**
	 * @param a
	 *            The literal set A.
	 * @param b
	 *            The literal set B.
	 * @return The difference A \ B.
	 */
	public static LiteralSet differenceOfLiteralSets(LiteralSet a, LiteralSet b) {

		return new LiteralSet(SetUtil.difference(a, b));

	}
	
	public static boolean doesPremiseContainAGroundLiteralThatIsNotInFactBase(Collection<Literal> factbase, Collection<Literal> premise) {
		for (Literal l : premise) {
			if (l.isGround() && !factbase.contains(l))
				return true;
		}
		return false;
	}
	
	public static boolean doesPremiseContainAGroundLiteralThatIsNotInFactBaseCWA(Collection<Literal> factbase, Collection<Literal> premise) {
		for (Literal l : premise) {
			if (!l.isGround())
				continue;
			if (l.isPositive()) {
				if (!factbase.contains(l))
					return true;
			}
			else {
				if (factbase.contains(l.clone().toggleNegation()))
					return true;
			}
		}
		return false;
	}
	
	public static boolean verifyThatGroundingEnablesPremise(Collection<Literal> factbase, Collection<Literal> premise, Map<VariableParam,LiteralParam> grounding) {
		for (Literal l : premise) {
			Literal lg = new Literal(l, grounding);
			if (factbase.contains(lg) != l.isPositive()) {
				System.err.println("Literal " + l + " in premise ground to " + lg + " does not follow from state: ");
				factbase.stream().sorted((l1,l2) -> l1.toString().compareTo(l2.toString())).forEach(lit -> System.out.println("\t" + lit));
				return false;
			}
		}
		return true;
	}

	public static boolean canLiteralBeUnifiedWithLiteralFromDatabase(Collection<Literal> set, Literal literal) {
		for (Literal candidate : set) {
			if (areLiteralsUnifiable(candidate, literal))
				return true;
		}
		return false;
	}

	public static boolean areLiteralsUnifiable(Literal l1, Literal l2) {
		if (!l1.getPropertyName().equals(l2.getPropertyName()))
			return false;
		List<LiteralParam> paramsOfL1 = l1.getParameters();
		List<LiteralParam> paramsOfL2 = l2.getParameters();
		for (int i = 0; i < paramsOfL1.size(); i++) {
			if (paramsOfL1.get(i) instanceof ConstantParam && paramsOfL2.get(i) instanceof ConstantParam && !paramsOfL1.get(i).equals(paramsOfL2.get(i)))
				return false;
		}
		return true;
	}

	public static LiteralParam parseParamName(String name) {
		boolean isConstant = false;
		if (name.contains("'")) {
			if (!name.startsWith("'") || !name.endsWith("'") || (name = name.substring(1, name.length() - 1)).contains("'"))
				throw new IllegalArgumentException("A parameter that contains simple quotes must contain EXACTLY two such quotes (one in the beginning, one in the end). Such a name indicates a constant!");
			isConstant = true;
		}
		Type type = null;
		if (name.contains(":")) {
			String[] parts = name.split(":");
			if (parts.length != 2)
				throw new IllegalArgumentException("The name of a parameter must contain at most one colon! A colon is used to separate the name from the type!");
			name = parts[0];
			type = new Type(parts[1]);
		}
		return isConstant ? new ConstantParam(name, type) : new VariableParam(name, type);
	}

	public static boolean evalEquality(Literal l) {
		List<LiteralParam> params = l.getParameters();
		if (!(params.get(0) instanceof ConstantParam) || !(params.get(1) instanceof ConstantParam))
			throw new IllegalArgumentException("Equality cannot be evaluated for non-constants!");
		return params.get(0).equals(params.get(1)) == l.isPositive();
	}

	public static CNFFormula evalEqualityLiteralsUnderUNA(CNFFormula set) {
		CNFFormula newFormula = new CNFFormula();

		for (Clause c : set) {
			Clause cNew = new Clause();
			for (Literal l : c) {
				if (l.getPropertyName().equals("=")) {
					List<LiteralParam> params = l.getParameters();
					if (params.get(0) instanceof ConstantParam && params.get(1) instanceof ConstantParam) {
						if (params.get(0).equals(params.get(1)) != l.isPositive())
							return new CNFFormula(new Monom("A & !A"));
					} else {
						cNew.add(l);
					}
				} else {
					cNew.add(l);
				}
			}
			if (!cNew.isEmpty())
				newFormula.add(cNew);
		}
		return newFormula;
	}
}
