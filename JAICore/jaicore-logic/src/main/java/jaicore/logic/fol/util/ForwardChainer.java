package jaicore.logic.fol.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.structure.LiteralSet;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;

/**
 * This algorithm answers the question for which groundings G of the variables
 * in $premise, G[$premise] follows from $factbase. This method does NOT adopt
 * the closed world assumption, i.e. !L in the premise can only be followed if L
 * is provably wrong in the factbase.
 *
 * @param factbase
 * @param conclusion
 * @return
 */
public class ForwardChainer extends AAlgorithm<ForwardChainingProblem, Collection<Map<VariableParam, LiteralParam>>> {

	private Logger logger = LoggerFactory.getLogger(ForwardChainer.class);
	private Monom conclusion;
	private Monom cwaRelevantNegativeLiterals; // contains the negative part of the conclusion IF CWA is active!
	private Monom factbase;

	private Literal chosenLiteral;
	private List<Map<VariableParam, LiteralParam>> possibleChoicesForLocalLiteral;
	private Monom remainingConclusion;
	private Map<VariableParam, LiteralParam> currentGroundingOfLocalLiteral;
	private Monom currentGroundRemainingConclusion;
	private ForwardChainer currentlyActiveSubFC;

	public ForwardChainer(final ForwardChainingProblem problem) {
		super(problem);
		if (problem.getConclusion().isEmpty()) {
			throw new IllegalArgumentException("Ill-defined forward chaining problem with empty conclusion!");
		}
	}

	@Override
	/**
	 * This is a recursive algorithm. It will only identify solutions for one of the
	 * literals in the conclusion and invoke a new instance of ForwardChainer on the
	 * rest.
	 */
	public AlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException {
		long start = System.currentTimeMillis();
		switch (this.getState()) {

		/* initialize the algorithm for the most promising literal */
		case CREATED:
			this.conclusion = this.getInput().getConclusion();
			assert !this.conclusion.isEmpty() : "The algorithm should not be invoked with an empty conclusion";
			this.factbase = this.getInput().getFactbase();
			this.logger.info("Computing substitution for {}-conclusion that enable forward chaining from factbase of size {}. Enable trace for more detailed output.", this.conclusion.size(), this.factbase.size());
			this.logger.trace("Conclusion is {}", this.conclusion);
			this.logger.trace("Factbase is {}", this.factbase);

			/* if CWA is active, store away all negative literals */
			if (this.getInput().isCwa()) {

				/* decompose conclusion in positive and negative literals */
				Monom positiveLiterals = new Monom();
				Monom negativeLiterals = new Monom();
				for (Literal l : this.conclusion) {
					if (l.isPositive()) {
						positiveLiterals.add(l);
					} else {
						negativeLiterals.add(l);
					}
				}
				this.conclusion = positiveLiterals;
				this.cwaRelevantNegativeLiterals = negativeLiterals;
			}

			/* select the literal that has the least options to be ground */
			int currentlyFewestOptions = Integer.MAX_VALUE;
			long timeToPrepareCWAVersion = System.currentTimeMillis();
			for (Literal nextLitealCandidate : this.conclusion) {
				this.checkAndConductTermination();
				this.logger.debug("Considering {} as next literal for grounding.", nextLitealCandidate);
				long candidateGroundingStart = System.currentTimeMillis();
				List<Map<VariableParam, LiteralParam>> choicesTmp = this.getGroundingsUnderWhichALiteralAppearsInFactBase(this.factbase, nextLitealCandidate, currentlyFewestOptions);
				this.logger.debug("Computation of {} groundings took {}ms.", choicesTmp.size(), System.currentTimeMillis() - candidateGroundingStart);
				if (choicesTmp.size() < currentlyFewestOptions) {
					this.chosenLiteral = nextLitealCandidate;
					this.possibleChoicesForLocalLiteral = choicesTmp;
					currentlyFewestOptions = choicesTmp.size();
					if (currentlyFewestOptions == 0) {
						break;
					}
				}
			}
			assert this.chosenLiteral != null : "No literal has been chosen";
			assert this.possibleChoicesForLocalLiteral != null : "List of possible choices for literal must not be null";
			this.remainingConclusion = new Monom();
			for (Literal l : this.conclusion) {
				if (!l.equals(this.chosenLiteral)) {
					this.remainingConclusion.add(l);
				}
			}
			long end = System.currentTimeMillis();
			this.logger.debug("Selected literal {} with still unbound params {} that can be ground in {} ways in {}ms.", this.chosenLiteral, this.chosenLiteral.getVariableParams(), this.possibleChoicesForLocalLiteral.size(),
					end - timeToPrepareCWAVersion);
			this.logger.info("Initialized FC algorithm within {}ms.", end - start);
			return this.activate();

		case ACTIVE:

			this.checkAndConductTermination();

			/* if a sub-process is running, get its result and combine it with our current grounding for the local literal */
			if (this.currentlyActiveSubFC != null) {
				this.logger.trace("Reuse currently active recursive FC as it may still have solutions ...");
				NextBindingFoundEvent event = this.currentlyActiveSubFC.nextBinding();
				if (event == null) {
					this.currentlyActiveSubFC = null;
				} else {
					Map<VariableParam, LiteralParam> subsolution = event.getGrounding();
					this.logger.debug("Identified recursively determined sub-solution {}", subsolution);
					Map<VariableParam, LiteralParam> solutionToReturn = new HashMap<>(subsolution);
					solutionToReturn.putAll(this.currentGroundingOfLocalLiteral);
					assert this.verifyThatGroundingEnablesConclusion(this.factbase, this.currentGroundRemainingConclusion, solutionToReturn);

					/* if CWA is activated, we have to recheck whether the negative literals are ok */
					if (this.getInput().isCwa() && this.doesCWADeductionFail(this.factbase, new LiteralSet(this.cwaRelevantNegativeLiterals, solutionToReturn))) {
						return new ForwardChainingFailedCWABindingEvent(this.getId());
					}
					this.logger.info("Computed binding {} for {}-conclusion within {}ms", solutionToReturn, this.conclusion.size(), System.currentTimeMillis() - start);
					return new NextBindingFoundEvent(this.getId(), solutionToReturn);
				}
			}

			/* if we reach this part, we need to determine the next grounding of the local predicate, which we want to check (and for which we may want to recurse) */
			this.logger.debug("Determine a new out of {} remaining groundings for {} to be analyzed.", this.possibleChoicesForLocalLiteral.size(), this.chosenLiteral);
			boolean foundAChoiceThatMightBeFeasible = false;
			while (!foundAChoiceThatMightBeFeasible && !this.possibleChoicesForLocalLiteral.isEmpty()) {
				this.checkAndConductTermination();
				this.currentGroundingOfLocalLiteral = this.possibleChoicesForLocalLiteral.get(0);
				this.possibleChoicesForLocalLiteral.remove(0);
				this.logger.debug("Considering choice {}", this.currentGroundingOfLocalLiteral);
				Monom modifiedRemainingConclusion = new Monom(this.remainingConclusion, this.currentGroundingOfLocalLiteral);
				this.logger.trace("Checking whether one of the ground remaining conclusion {} is not in the state.", modifiedRemainingConclusion);
				if (!this.doesConclusionContainAGroundLiteralThatIsNotInFactBase(this.factbase, modifiedRemainingConclusion)) {
					foundAChoiceThatMightBeFeasible = true;
					this.currentGroundRemainingConclusion = modifiedRemainingConclusion;
					break;
				}
			}
			this.logger.debug("Selected grounding {}. {} possible other groundings remain.", this.currentGroundingOfLocalLiteral, this.possibleChoicesForLocalLiteral.size());

			/* if no (more) groundings are possible for this literal and no feasible grounding was detected, return the algorithm finished event */
			if (!foundAChoiceThatMightBeFeasible) {
				assert this.possibleChoicesForLocalLiteral.isEmpty() : "Collection of possible choices should be empty when no grounding was chosen!";
				this.logger.debug("Finishing process for {}-conclusion since no (more) grounding is avilable for predicate {}.", this.conclusion.size(), this.chosenLiteral);
				return this.terminate();
			}

			/* if the conclusion has size 1, return the current candidate. Otherwise recurse */
			if (this.currentGroundRemainingConclusion.isEmpty()) {
				return new NextBindingFoundEvent(this.getId(), this.currentGroundingOfLocalLiteral);
			} else {
				this.logger.debug("Recurse to {}-conclusion", this.currentGroundRemainingConclusion.size());
				ForwardChainingProblem subProblem = new ForwardChainingProblem(this.factbase, this.currentGroundRemainingConclusion, this.getInput().isCwa());
				long startRecursiveCall = System.currentTimeMillis();
				this.logger.debug("Finished recursion of {}-conclusion. Computation took {}ms", this.currentGroundRemainingConclusion.size(), System.currentTimeMillis() - startRecursiveCall);
				this.currentlyActiveSubFC = new ForwardChainer(subProblem);
				return new ForwardChainerRecursionEvent(this.getId(), this.chosenLiteral, this.currentGroundRemainingConclusion);
			}

		default:
			throw new IllegalStateException("Don't know how to behave in state " + this.getState());
		}
	}

	@Override
	public Collection<Map<VariableParam, LiteralParam>> call() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException {
		Collection<Map<VariableParam, LiteralParam>> mappings = new ArrayList<>();
		NextBindingFoundEvent e;
		while ((e = this.nextBinding()) != null) {
			this.logger.info("Adding solution grounding {} to output set.", e.getGrounding());
			mappings.add(e.getGrounding());
		}
		return mappings;
	}

	public NextBindingFoundEvent nextBinding() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException {
		while (this.hasNext()) {
			AlgorithmEvent e = this.nextWithException();
			if (e instanceof NextBindingFoundEvent) {
				return (NextBindingFoundEvent) e;
			}
		}
		return null;
	}

	public Collection<Map<VariableParam, LiteralParam>> getSubstitutionsThatEnableForwardChaining(final Collection<Literal> factbase, final Collection<Literal> conclusion) {
		return this.getSubstitutionsThatEnableForwardChaining(factbase, new ArrayList<>(conclusion));
	}

	public boolean doesConclusionContainAGroundLiteralThatIsNotInFactBase(final Collection<Literal> factbase, final Collection<Literal> conclusion) {
		for (Literal l : conclusion) {
			if (l.isGround() && !factbase.contains(l)) {
				return true;
			}
		}
		return false;
	}

	/**
	 *
	 * @param factbase
	 * @param conclusion
	 * @return true iff the conclusion contains a positive ground literal that is
	 *         NOT in the factbase or a negative ground literal that positively
	 *         occurs in the factbase
	 */
	public boolean doesCWADeductionFail(final Collection<Literal> factbase, final Collection<Literal> conclusion) {
		for (Literal l : conclusion) {
			if (!l.isGround()) {
				continue;
			}
			if (l.isPositive()) {
				if (!factbase.contains(l)) {
					return true;
				}
			} else {
				if (factbase.contains(l.clone().toggleNegation())) {
					return true;
				}
			}
		}
		return false;
	}

	public List<Map<VariableParam, LiteralParam>> getGroundingsUnderWhichALiteralAppearsInFactBase(final Collection<Literal> factbase, final Literal l, final int maxSubstitutions) {

		List<VariableParam> openParams = l.getVariableParams();

		/*
		 * if there are no open params, we do not need to make decisions here, so just
		 * compute subsolutions
		 */
		this.logger.debug("Compute possible sub-groundings of the open parameters.");
		long start = System.currentTimeMillis();
		List<Map<VariableParam, LiteralParam>> choices = new ArrayList<>();
		if (openParams.isEmpty()) {
			choices.add(new HashMap<>());
		}

		/*
		 * otherwise, select literal from the factbase that could be used for
		 * unification
		 */
		else {
			for (Literal fact : factbase) {
				if (!fact.getPropertyName().equals(l.getPropertyName()) || fact.isPositive() != l.isPositive()) {
					continue;
				}
				this.logger.trace("Considering known literal {} as a literal that can be used for grounding", fact);
				List<LiteralParam> factParams = fact.getParameters(); // should only contain constant params
				List<LiteralParam> nextLiteralParams = l.getParameters();
				Map<VariableParam, LiteralParam> submap = new HashMap<>();

				/* create a substitution that grounds the rest of the literal */
				boolean paramsCanBeMatched = true;
				for (int i = 0; i < factParams.size(); i++) {
					if (nextLiteralParams.get(i) instanceof VariableParam) {
						submap.put((VariableParam) nextLiteralParams.get(i), factParams.get(i));
					} else if (!nextLiteralParams.get(i).equals(factParams.get(i))) {
						paramsCanBeMatched = false;
						break;
					}
				}
				if (!paramsCanBeMatched) {
					continue;
				}
				this.logger.trace("Adding {} as a possible such grounding.", submap);
				choices.add(submap);
				if (choices.size() >= maxSubstitutions) {
					this.logger.debug("Reached maximum number {} of required substitutions. Returning what we have so far.", maxSubstitutions);
					return choices;
				}
			}
		}
		this.logger.debug("Done. Computation of {} groundings took {}ms", choices.size(), System.currentTimeMillis() - start);
		return choices;
	}

	public boolean verifyThatGroundingEnablesConclusion(final Collection<Literal> factbase, final Collection<Literal> conclusion, final Map<VariableParam, LiteralParam> grounding) {
		for (Literal l : conclusion) {
			Literal lg = new Literal(l, grounding);
			if (factbase.contains(lg) != l.isPositive()) {
				this.logger.error("Literal {} in conclusion ground to {} does not follow from state: ", l, lg);
				factbase.stream().sorted((l1, l2) -> l1.toString().compareTo(l2.toString())).forEach(lit -> this.logger.info("\t{}", lit));
				return false;
			}
		}
		return true;
	}
}
