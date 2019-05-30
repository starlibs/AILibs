package jaicore.logic.fol.algorithms.resolution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.sets.SetUtil;
import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.Clause;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.structure.VariableParam;

/**
 * This solver tries to solver PL1 formulas in CNF where all variables are existentially quantified
 *
 * @author Felix
 *
 */
public abstract class Solver {

	private static final Logger logger = LoggerFactory.getLogger(Solver.class);

	private final Set<CNFFormula> formulas = new HashSet<>();
	private final Map<CNFFormula, CNFFormula> internalRepresentationOfFormulas = new HashMap<>();
	private ResolutionTree tree; // stores the resolution tree

	private int varCounter = 1;

	public Set<CNFFormula> getFormulas() {
		return this.formulas;
	}

	public Map<CNFFormula, CNFFormula> getInternalRepresentationOfFormulas() {
		return this.internalRepresentationOfFormulas;
	}

	public void addFormula(final CNFFormula formula) {
		this.formulas.add(formula);

		/* make variable names in all clauses disjoint */
		CNFFormula disAmbFormula = new CNFFormula();
		for (Clause c : formula) {
			Set<VariableParam> paramsInC = c.getVariableParams();
			Map<VariableParam, LiteralParam> substitution = new HashMap<>();
			for (VariableParam p : paramsInC) {
				substitution.put(p, new VariableParam("c" + this.varCounter + "_" + p.getName()));
			}
			disAmbFormula.add(new Clause(c, substitution));
			this.varCounter++;
		}
		this.internalRepresentationOfFormulas.put(formula, disAmbFormula);
	}

	public void removeFormula(final CNFFormula formula) {
		this.formulas.remove(formula);
		this.internalRepresentationOfFormulas.remove(formula);
	}

	public boolean isSatisfiable(final CNFFormula formula) throws InterruptedException {
		CNFFormula internalRepresentation = this.internalRepresentationOfFormulas.get(formula);
		CNFFormula resultOfResolution = this.performResolutionUntilEmptyClauseIsFound(internalRepresentation, internalRepresentation);
		this.internalRepresentationOfFormulas.put(formula, resultOfResolution);
		return !resultOfResolution.contains(new HashSet<>());
	}

	/**
	 * is f & g satisfiable if resolution must take in each step at least one clause from g (and resolvents produced)?
	 *
	 * @param formula
	 * @param formulaToChooseAtLeastOneLiteralFrom
	 * @return
	 */
	public boolean isSatisfiable(final CNFFormula formula, final CNFFormula formulaToChooseAtLeastOneLiteralFrom) throws InterruptedException {
		CNFFormula internalRepresentationOfFormula1 = this.internalRepresentationOfFormulas.get(formula);
		CNFFormula internalRepresentationOfFormula2 = this.internalRepresentationOfFormulas.get(formulaToChooseAtLeastOneLiteralFrom);
		CNFFormula joint = new CNFFormula();
		joint.addAll(internalRepresentationOfFormula1);
		joint.addAll(internalRepresentationOfFormula2);
		CNFFormula resultOfResolution = this.performResolutionUntilEmptyClauseIsFound(joint, internalRepresentationOfFormula2);
		return !resultOfResolution.contains(new HashSet<>());
	}

	public boolean isSatisfiable() throws InterruptedException {
		CNFFormula jointFormula = new CNFFormula();
		for (CNFFormula formula : this.formulas) {
			jointFormula.addAll(this.internalRepresentationOfFormulas.get(formula));
		}
		CNFFormula resultOfResolution = this.performResolutionUntilEmptyClauseIsFound(jointFormula, jointFormula);
		return !resultOfResolution.contains(new HashSet<>());
	}

	protected CNFFormula performResolutionUntilEmptyClauseIsFound(final CNFFormula formula, final CNFFormula formulaToChooseAtLeastOneLiteralFrom) throws InterruptedException {

		/* create start state with substituted formula */
		Set<ResolutionPair> candidates = new HashSet<>();
		candidates.addAll(this.getPossibleResolutionPairs(formulaToChooseAtLeastOneLiteralFrom));
		for (Clause c : formula) {
			candidates.addAll(this.getPossibleResolutionPairs(formulaToChooseAtLeastOneLiteralFrom, c));
		}

		/* create memory */
		this.tree = new ResolutionTree(formula);

		CNFFormula currentFormula = new CNFFormula(formula);
		ResolutionPair nextPair;
		logger.info("Starting resolution for formula {} using in each iteration literals from {}", formula, formulaToChooseAtLeastOneLiteralFrom);
		logger.info("The initial set of candidates is {}", candidates);
		while ((nextPair = this.getNextPair(candidates)) != null) {
			ResolutionStep step = this.performResolutionStepForPair(nextPair);
			if (step == null) {
				continue;
			}
			Clause resolvent = step.getR();
			logger.debug("Size is {}: Resolving {} with {} on literal {} with unifier {}. Resolvent is: {}", candidates.size(), nextPair.getC1(), nextPair.getC2(),
					nextPair.getL1().getProperty(), step.getUnificator(), resolvent);
			if (nextPair.getC1().isTautological() || nextPair.getC2().isTautological()) {
				logger.error("Resolved tautological clause!");
			}
			if (resolvent.isEmpty()) { // cancel if we deduced the empty clause
				logger.debug("Found empty clause, canceling process.");
				currentFormula.add(resolvent);
				this.tree.addResolutionStep(step);
				return currentFormula;
			}

			if (this.isClauseAlreadyContainedInFormula(currentFormula, resolvent)) {
				logger.debug("Ignoring resolvent, because it is already contained in the known formula.");
				continue;
			}

			this.tree.addResolutionStep(step); // memorize this step

			if (resolvent.isTautological()) {
				logger.debug("Not considering any clauses producible with this resolvent, because it is tautological.");
				continue;
			}

			logger.debug("Added resolvent {} to formula, which has now size {}.", resolvent, currentFormula.size());
			currentFormula.add(resolvent);

			/*
			 * if this resolvent is not tautological, create new candidates that have become available by this resolvent
			 */
			List<ResolutionPair> successors = this.getPossibleResolutionPairs(currentFormula, resolvent);
			candidates.addAll(this.getAdmissiblePairs(successors));
		}
		return currentFormula;
	}

	public ResolutionTree getTree() {
		return this.tree;
	}

	protected ResolutionPair getNextPair(final Set<ResolutionPair> candidates) {

		/* try to find a pair with unit clause */
		List<ResolutionPair> pairsWithUnitClause = candidates.stream().filter(p -> (p.getC1().size() == 1 || p.getC2().size() == 1)).collect(Collectors.toList());
		if (!pairsWithUnitClause.isEmpty()) {
			ResolutionPair pair = pairsWithUnitClause.get(0);
			candidates.remove(pair);
			return pair;
		}

		/* now minimize for s.th. */
		Optional<ResolutionPair> min = candidates.stream().min((p1, p2) -> (p1.getC1().size() + p1.getC2().size()) - (p2.getC1().size() + p2.getC2().size()));
		if (!min.isPresent()) {
			return null;
		}
		ResolutionPair pair = min.get();
		candidates.remove(pair);
		return pair;
	}

	protected List<ResolutionPair> getPossibleResolutionPairs(final CNFFormula formula) {
		List<ResolutionPair> pairs = new LinkedList<>();
		Set<List<Clause>> candidates = SetUtil.getAllPossibleSubsetsWithSize(formula, 2).stream().map(c -> new LinkedList<>(c)).collect(Collectors.toSet());
		for (List<Clause> pair : candidates) {
			Clause c1 = pair.get(0);
			Clause c2 = pair.get(1);
			boolean invert = c1.size() > c2.size();
			pairs.addAll(this.getPossibleResolutionsPairsOfClauses(invert ? c2 : c1, invert ? c1 : c2));
		}
		return pairs;
	}

	protected abstract List<ResolutionPair> getAdmissiblePairs(List<ResolutionPair> pairs);

	protected List<ResolutionPair> getPossibleResolutionPairs(final CNFFormula formula, final Clause c2) {
		logger.debug("Computing all resolution pairs between formula {} and clause {}", formula, c2);
		List<ResolutionPair> pairs = new LinkedList<>();
		for (Clause c1 : formula) {
			boolean invert = c1.size() > c2.size();
			pairs.addAll(this.getPossibleResolutionsPairsOfClauses(invert ? c2 : c1, invert ? c1 : c2));
		}
		return pairs;
	}

	protected Set<Clause> getClausesWithSamePredicates(final CNFFormula formula, final Clause c2) {
		Set<Clause> clausesWithSamePredicates = new HashSet<>();
		Set<String> predicatesInC2 = c2.toPropositionalSet();
		for (Clause c1 : formula) {
			if (c1.size() != c2.size()) {
				continue;
			}
			if (c1.toPropositionalSet().equals(predicatesInC2)) {
				clausesWithSamePredicates.add(c1);
			}
		}
		return clausesWithSamePredicates;
	}

	protected boolean isClauseAlreadyContainedInFormula(final CNFFormula formula, final Clause c) throws InterruptedException {
		if (formula.contains(c)) {
			return true;
		}
		Set<Clause> possibleMatchings = this.getClausesWithSamePredicates(formula, c);
		int numberOfVarsInC = c.getVariableParams().size();
		for (Clause candidate : possibleMatchings) {
			Set<VariableParam> paramsOfCandidate = candidate.getVariableParams();
			Clause clauseWithLessParams = ((numberOfVarsInC < paramsOfCandidate.size()) ? c : candidate);
			Clause clauseWithMoreParams = clauseWithLessParams == c ? candidate : c;
			for (Map<VariableParam, VariableParam> map : SetUtil.allTotalMappings(clauseWithMoreParams.getVariableParams(), clauseWithLessParams.getVariableParams())) {
				Clause clauseWithMoreParamsMapped = new Clause(clauseWithMoreParams, map);
				if (clauseWithMoreParamsMapped.equals(clauseWithLessParams)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 *
	 * @param c1
	 *            Note that c1 MUST be the smaller clause by convention
	 * @param c2
	 * @return
	 */
	protected List<ResolutionPair> getPossibleResolutionsPairsOfClauses(final Clause c1, final Clause c2) {
		List<ResolutionPair> pairs = new ArrayList<>();
		if (c1.size() > c2.size()) {
			logger.error("Error, first clause bigger than second!");
		}

		/*
		 * for each of the literals in c1, find literals in c2 that can be used for resolution
		 */
		for (Literal l1 : c1) {
			for (Literal l2 : c2) {
				if ((l1.getPropertyName().equals(l2.getPropertyName()) && l1.isPositive() != l2.isPositive())) {
					pairs.add(new ResolutionPair(c1, c2, l1, l2));
				}
			}
		}
		logger.debug("Possible resolution pairs for clauses {} and {} are {}", c1, c2, pairs);
		return pairs;
	}

	protected ResolutionStep performResolutionStepForPair(final ResolutionPair pair) {

		/* get unifier for the two clauses */
		Map<VariableParam, LiteralParam> unifier = this.getUnificatorForLiterals(pair.getL1(), pair.getL2());
		if (unifier == null) {
			return null;
		}

		/* literals are unifiable, now compute resulting resolvent */
		Clause basicResolvent = new Clause();
		for (Literal l : pair.getC1()) {
			if (l == pair.getL1()) {
				continue;
			}
			basicResolvent.add(new Literal(l, unifier));
		}
		for (Literal l : pair.getC2()) {
			if (l == pair.getL2()) {
				continue;
			}
			basicResolvent.add(new Literal(l, unifier));
		}

		/*
		 * now make variables in this new resolve unique by adding an underscore to each of the variables
		 */
		Map<VariableParam, LiteralParam> substitution = new HashMap<>();
		for (VariableParam var : basicResolvent.getVariableParams()) {
			substitution.put(var, new VariableParam("_" + var.getName()));
		}
		Clause resolvent = new Clause(basicResolvent, substitution);

		/* add this resolution step to the possible steps */
		return new ResolutionStep(pair, resolvent, unifier);
	}

	/**
	 * Performs Robinsons's unification
	 *
	 * @param l1
	 * @param l2
	 * @return
	 */
	protected Map<VariableParam, LiteralParam> getUnificatorForLiterals(final Literal l1, final Literal l2) {
		List<LiteralParam> p1 = new LinkedList<>(l1.getParameters());
		List<LiteralParam> p2 = new LinkedList<>(l2.getParameters());
		if (p1.size() != p2.size()) {
			return null;
		}
		Map<VariableParam, LiteralParam> unifier = new HashMap<>();
		for (int i = 0; i < p1.size(); i++) {
			LiteralParam v1 = p1.get(i);
			LiteralParam v2 = p2.get(i);

			/* not unifiable if two different constants must be unified */
			if (v1 instanceof ConstantParam && v2 instanceof ConstantParam && !v1.equals(v2)) {
				return null;
			} else if (v1 instanceof VariableParam && v2 instanceof ConstantParam) {
				unifier.put((VariableParam) v1, v2);
				for (Entry<VariableParam,LiteralParam> unificationEntry: unifier.entrySet()) {
					if (unificationEntry.getValue().equals(v1)) {
						unifier.put(unificationEntry.getKey(), v2);
					}
				}
			}

			else if (v2 instanceof VariableParam && v1 instanceof ConstantParam) {
				unifier.put((VariableParam) v2, v1);
				for (VariableParam key : unifier.keySet()) {
					if (unifier.get(key).equals(v2)) {
						unifier.put(key, v1);
					}
				}
			}

			else {
				if (!v1.equals(v2)) {
					unifier.put((VariableParam) v2, v1);
				}
				for (VariableParam key : unifier.keySet()) {
					if (unifier.get(key).equals(v2)) {
						unifier.put(key, v1);
					}
				}
			}
		}
		return unifier;
	}
}
