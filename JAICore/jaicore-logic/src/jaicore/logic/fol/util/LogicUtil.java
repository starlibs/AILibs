package jaicore.logic.fol.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger logger = LoggerFactory.getLogger(LogicUtil.class);
	/* set assertionsActive to true if they are enabled */
	private static final boolean assertionsActive;
	static {
		boolean aTmp = false;
		assert aTmp = true;
		assertionsActive = aTmp;
	}
	
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
	
	public static Collection<Map<VariableParam, LiteralParam>> getSubstitutionsThatEnableForwardChainingUnderCWA(Collection<Literal> factbase, Collection<Literal> premise) {
		
		/* decompose premise in positive and negative literals */
		LiteralSet positiveLiterals = new LiteralSet();
		LiteralSet negativeLiterals = new LiteralSet();
		for (Literal l : premise) {
			if (l.isPositive())
				positiveLiterals.add(l);
			else
				negativeLiterals.add(l);
		}
		
		/* get groundings for which positive literals hold and then check each of those groundings on conformity with negative literals */
		 Collection<Map<VariableParam, LiteralParam>> groundings = getSubstitutionsThatEnableForwardChaining(factbase, positiveLiterals);
		 if (logger.isTraceEnabled()) {
			 groundings.forEach(g -> {
				 LiteralSet groundPositivePremise = new LiteralSet(positiveLiterals, g);
				 for (Literal l : groundPositivePremise)
					 assert factbase.contains(l) : "The factbase should contain " + l + " according to forward chaining, but it does not!";
				 logger.trace("Grounding {} induces premise {}, which holds.", g, groundPositivePremise);
			 });
		 }
		 return groundings.stream().filter(grounding ->  {
			 LiteralSet groundNegativeLiterals = new LiteralSet(negativeLiterals, grounding);
			 boolean negativeLiteralIsPositivelyContainedInFactbase = doesPremiseContainAGroundLiteralThatIsNotInFactBaseCWA(factbase, groundNegativeLiterals);
			 return !negativeLiteralIsPositivelyContainedInFactbase;
		 }).collect(Collectors.toList());
	}
	
	public static Collection<Map<VariableParam, LiteralParam>> getSubstitutionsThatEnableForwardChaining(Collection<Literal> factbase, Collection<Literal> premise) {
		return getSubstitutionsThatEnableForwardChaining(factbase, new ArrayList<>(premise));
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
	
	/**
	 * This method answers the question for which groundings G of the variables in $premise, G[$premise] follows from $factbase.
	 * This method does NOT adopt the closed world assumption, i.e. !L in the premise can only be followed if L is provably wrong in the factbase. 
	 * 
	 * @param factbase
	 * @param premise
	 * @return
	 */
	private static Collection<Map<VariableParam, LiteralParam>> getSubstitutionsThatEnableForwardChaining(Collection<Literal> factbase, List<Literal> premise) {
		
		logger.info("Computing substitution for {} that enable forward chaining from {}", premise, factbase);
		Collection<Map<VariableParam, LiteralParam>> mappings = new HashSet<>();

		/* if the premise is empty, add the empty mapping */
		if (premise.isEmpty()) {
			mappings.add(new HashMap<>());
			return mappings;
		}

		/* in any other case, select a literal and compute remaining premise, which is the premise minus the first element, minus all other ground elements */
		Literal nextLiteral = premise.get(0);
		List<Literal> remainingPremise = new ArrayList<>();
		for (int i = 1; i < premise.size(); i++) {
			if (!premise.get(i).getVariableParams().isEmpty())
				remainingPremise.add(premise.get(i));
		}
		List<VariableParam> openParams = nextLiteral.getVariableParams();
		logger.trace("Try to see whether params {} can be ground such that literal {} holds.", openParams, nextLiteral);

		/* if there are no open params, we do not need to make decisions here, so just compute subsolutions */
		Collection<Map<VariableParam, LiteralParam>> choices = new HashSet<>();
		if (openParams.isEmpty()) {
			choices.add(new HashMap<>());
		}

		/* otherwise, select literal from the factbase that could be used for unification */
		else {
			for (Literal fact : factbase) {
				if (!fact.getPropertyName().equals(nextLiteral.getPropertyName()) || fact.isPositive() != nextLiteral.isPositive())
					continue;
				logger.trace("Considering known literal {} as a literal that can be used for grounding", fact);
				List<LiteralParam> factParams = fact.getParameters(); // should only contain constant params
				List<LiteralParam> nextLiteralParams = nextLiteral.getParameters();
				Map<VariableParam, LiteralParam> submap = new HashMap<>();

				/* create a substitution that grounds the rest of the literal */
				boolean paramsCanBeMatched = true;
				for (int i = 0; i < factParams.size(); i++) {
					if (nextLiteralParams.get(i) instanceof VariableParam) {
						submap.put((VariableParam) nextLiteralParams.get(i), factParams.get(i));
					}
					else if (!nextLiteralParams.get(i).equals(factParams.get(i))) {
						paramsCanBeMatched = false;
						break;
					}
				}
				if (!paramsCanBeMatched)
					continue;
				logger.trace("Adding {} as a possible such grounding.", submap);
				choices.add(submap);
			}
		}

		/* now apply the different possible choices substitution to the remaining premise and compute possible submappings */
		for (Map<VariableParam, LiteralParam> submap : choices) {
			logger.debug("Considering choice {}", submap);
			Monom modifiedRemainingPremise = new Monom(remainingPremise, submap);
			
			/* if there is a ground literal in the modified remaining premise that is not in the fact base, skip this option */
			logger.trace("Checking whether one of the ground remaining premise {} is not in the state.", modifiedRemainingPremise);
			if (doesPremiseContainAGroundLiteralThatIsNotInFactBase(factbase, modifiedRemainingPremise))
				continue;
			
			/* otherwise recurse */
			Collection<Map<VariableParam, LiteralParam>> subsolutions = getSubstitutionsThatEnableForwardChaining(factbase, modifiedRemainingPremise);
			for (Map<VariableParam, LiteralParam> subsolution : subsolutions) {
				logger.trace("Identified sub-solution {}", subsolution);
				Map<VariableParam, LiteralParam> solutionToReturn = new HashMap<>(subsolution);
				solutionToReturn.putAll(submap);
				assert verifyThatGroundingEnablesPremise(factbase, modifiedRemainingPremise, solutionToReturn);
				mappings.add(solutionToReturn);
			}
		}
		logger.info("Finished computation of substitution for {} that enable forward chaining from {}: {}", premise, factbase, mappings);
		return mappings;
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
