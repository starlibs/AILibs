package jaicore.logic.fol.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.DelayedCancellationCheckException;
import jaicore.basic.algorithm.exceptions.DelayedTimeoutCheckException;
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

//	private Collection<Map<VariableParam, LiteralParam>> mappings = new HashSet<>();
	private Literal chosenLiteral;
	private List<Map<VariableParam, LiteralParam>> possibleChoicesForLocalLiteral;
	private Monom remainingConclusion;
	private Map<VariableParam, LiteralParam> currentGroundingOfLocalLiteral;
	private Monom currentGroundRemainingConclusion;
	private ForwardChainer currentlyActiveSubFC;

	public ForwardChainer(ForwardChainingProblem problem) {
		super(problem);
		assert !problem.getConclusion().isEmpty() : "Ill-defined forward chaining problem with empty conclusion!";
	}

	@Override
	/**
	 * This is a recursive algorithm. It will only identify solutions for one of the
	 * literals in the conclusion and invoke a new instance of ForwardChainer on the
	 * rest.
	 */
	public AlgorithmEvent nextWithException()
			throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException {
		long start = System.currentTimeMillis();
		try {
			switch (getState()) {

			/* initialize the algorithm for the most promising literal */
			case created: {
				conclusion = getInput().getConclusion();
				assert !conclusion.isEmpty() : "The algorithm should not be invoked with an empty conclusion";
				factbase = getInput().getFactbase();
				logger.info(
						"Computing substitution for {}-conclusion that enable forward chaining from factbase of size {}. Enable trace for more detailed output.",
						conclusion.size(), factbase.size());
				logger.trace("Conclusion is {}", conclusion);
				logger.trace("Factbase is {}", factbase);

				/* if CWA is active, store away all negative literals */
				if (getInput().isCwa()) {

					/* decompose conclusion in positive and negative literals */
					Monom positiveLiterals = new Monom();
					Monom negativeLiterals = new Monom();
					for (Literal l : conclusion) {
						if (l.isPositive())
							positiveLiterals.add(l);
						else
							negativeLiterals.add(l);
					}
					conclusion = positiveLiterals;
					cwaRelevantNegativeLiterals = negativeLiterals;
				}

				/* select the literal that has the least options to be ground */
				int currentlyFewestOptions = Integer.MAX_VALUE;
				long timeToPrepareCWAVersion = System.currentTimeMillis();
				for (Literal nextLitealCandidate : conclusion) {
					checkAndConductTermination();
					logger.debug("Considering {} as next literal for grounding.", nextLitealCandidate);
					long candidateGroundingStart = System.currentTimeMillis();
					List<Map<VariableParam, LiteralParam>> choicesTmp = getGroundingsUnderWhichALiteralAppearsInFactBase(
							factbase, nextLitealCandidate, currentlyFewestOptions);
					logger.debug("Computation of {} groundings took {}ms.", choicesTmp.size(),
							System.currentTimeMillis() - candidateGroundingStart);
					if (choicesTmp.size() < currentlyFewestOptions) {
						chosenLiteral = nextLitealCandidate;
						possibleChoicesForLocalLiteral = choicesTmp;
						currentlyFewestOptions = choicesTmp.size();
						if (currentlyFewestOptions == 0)
							break;
					}
				}
				assert chosenLiteral != null : "No literal has been chosen";
				assert possibleChoicesForLocalLiteral != null : "List of possible choices for literal must not be null";
				remainingConclusion = new Monom();
				for (Literal l : conclusion)
					if (!l.equals(chosenLiteral))
						remainingConclusion.add(l);
				long end = System.currentTimeMillis();
				logger.debug("Selected literal {} with still unbound params {} that can be ground in {} ways in {}ms.",
						chosenLiteral, chosenLiteral.getVariableParams(), possibleChoicesForLocalLiteral.size(),
						end - timeToPrepareCWAVersion);
				logger.info("Initialized FC algorithm within {}ms.", end - start);
				return activate();
			}

			case active: {

				checkAndConductTermination();
				/*
				 * if a sub-process is running, get its result and combine it with our current
				 * grounding for the local literal
				 */
				if (currentlyActiveSubFC != null) {
					logger.trace("Reuse currently active recursive FC as it may still have solutions ...");
					NextBindingFoundEvent event = currentlyActiveSubFC.nextBinding();
					if (event == null) {
						currentlyActiveSubFC = null;
					} else {
						Map<VariableParam, LiteralParam> subsolution = event.getGrounding();
						logger.debug("Identified recursively determined sub-solution {}", subsolution);
						Map<VariableParam, LiteralParam> solutionToReturn = new HashMap<>(subsolution);
						solutionToReturn.putAll(currentGroundingOfLocalLiteral);
						assert verifyThatGroundingEnablesConclusion(factbase, currentGroundRemainingConclusion,
								solutionToReturn);
						// logger.debug("Finished consistency check for choice {} in {}ms.",
						// currentGroundingOfLocalLiteral, System.currentTimeMillis() );
						// logger.debug("Done. Recursive consistency check of {} choices for the
						// {}-premise took {}ms and returned {} mappings.",
						// possibleChoicesForLocalLiteral.size(), remainingConclusion.size(),
						// System.currentTimeMillis() - start,
						// mappings.size());
						// logger.info("Finished computation of substitution for {} that enable forward
						// chaining from {}: {}", premise, factbase, mappings);

						/*
						 * if CWA is activated, we have to recheck whether the negative literals are ok
						 */
						if (getInput().isCwa() && doesCWADeductionFail(factbase,
								new LiteralSet(cwaRelevantNegativeLiterals, solutionToReturn)))
							return new ForwardChainingFailedCWABindingEvent(getId());
						logger.info("Computed binding {} for {}-conclusion within {}ms", solutionToReturn,
								conclusion.size(), System.currentTimeMillis() - start);
						return new NextBindingFoundEvent(getId(), solutionToReturn);
					}
				}

				/*
				 * if we reach this part, we need to determine the next grounding of the local
				 * predicate, which we want to check (and for which we may want to recurse)
				 */
				logger.debug("Determine a new out of {} remaining groundings for {} to be analyzed.",
						possibleChoicesForLocalLiteral.size(), chosenLiteral);
				boolean foundAChoiceThatMightBeFeasible = false;
				while (!foundAChoiceThatMightBeFeasible && !possibleChoicesForLocalLiteral.isEmpty()) {
					checkAndConductTermination();
					currentGroundingOfLocalLiteral = possibleChoicesForLocalLiteral.get(0);
					possibleChoicesForLocalLiteral.remove(0);
					logger.debug("Considering choice {}", currentGroundingOfLocalLiteral);
					Monom modifiedRemainingConclusion = new Monom(remainingConclusion, currentGroundingOfLocalLiteral);
					logger.trace("Checking whether one of the ground remaining conclusion {} is not in the state.",
							modifiedRemainingConclusion);
					if (!doesConclusionContainAGroundLiteralThatIsNotInFactBase(factbase,
							modifiedRemainingConclusion)) {
						foundAChoiceThatMightBeFeasible = true;
						currentGroundRemainingConclusion = modifiedRemainingConclusion;
						break;
					}
				}
				logger.debug("Selected grounding {}. {} possible other groundings remain.",
						currentGroundingOfLocalLiteral, possibleChoicesForLocalLiteral.size());

				/*
				 * if no (more) groundings are possible for this literal and no feasible
				 * grounding was detected, return the algorithm finished event
				 */
				if (!foundAChoiceThatMightBeFeasible) {
					assert possibleChoicesForLocalLiteral
							.isEmpty() : "Collection of possible choices should be empty when no grounding was chosen!";
					logger.debug(
							"Finishing process for {}-conclusion since no (more) grounding is avilable for predicate {}.",
							conclusion.size(), chosenLiteral);
					return terminate();
				}

				/*
				 * if the conclusion has size 1, return the current candidate. Otherwise recurse
				 */
				if (currentGroundRemainingConclusion.isEmpty()) {
					return new NextBindingFoundEvent(getId(), currentGroundingOfLocalLiteral);
				} else {
					logger.debug("Recurse to {}-conclusion", currentGroundRemainingConclusion.size());
					ForwardChainingProblem subProblem = new ForwardChainingProblem(factbase,
							currentGroundRemainingConclusion, getInput().isCwa());
					long startRecursiveCall = System.currentTimeMillis();
					// Collection<Map<VariableParam, LiteralParam>> subsolutions =
					// getSubstitutionsThatEnableForwardChaining(factbase,
					// currentGroundRemainingConclusion);
					logger.debug("Finished recursion of {}-conclusion. Computation took {}ms",
							currentGroundRemainingConclusion.size(), System.currentTimeMillis() - startRecursiveCall);
					currentlyActiveSubFC = new ForwardChainer(subProblem);
					return new ForwardChainerRecursionEvent(getId(), this.chosenLiteral, currentGroundRemainingConclusion);
				}
			}

			default:
				throw new IllegalStateException("Don't know how to behave in state " + getState());
			}
		} catch (DelayedTimeoutCheckException e) {
			e.printStackTrace();
			throw e.getException();
		} catch (DelayedCancellationCheckException e) {
			e.printStackTrace();
			throw e.getException();
		}

	}

	@Override
	public Collection<Map<VariableParam, LiteralParam>> call() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException {
		Collection<Map<VariableParam, LiteralParam>> mappings = new ArrayList<>();
		NextBindingFoundEvent e;
		while ((e = nextBinding()) != null) {
			logger.info("Adding solution grounding {} to output set.", e.getGrounding());
			mappings.add(e.getGrounding());
		}
		return mappings;
	}

	public NextBindingFoundEvent nextBinding() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException  {
		while (this.hasNext()) {
			AlgorithmEvent e = nextWithException();
			if (e instanceof NextBindingFoundEvent)
				return (NextBindingFoundEvent) e;
		}
		return null;
	}

	public Collection<Map<VariableParam, LiteralParam>> getSubstitutionsThatEnableForwardChaining(
			Collection<Literal> factbase, Collection<Literal> conclusion) {
		return getSubstitutionsThatEnableForwardChaining(factbase, new ArrayList<>(conclusion));
	}

	public boolean doesConclusionContainAGroundLiteralThatIsNotInFactBase(Collection<Literal> factbase,
			Collection<Literal> conclusion) {
		for (Literal l : conclusion) {
			if (l.isGround() && !factbase.contains(l))
				return true;
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
	public boolean doesCWADeductionFail(Collection<Literal> factbase, Collection<Literal> conclusion) {
		for (Literal l : conclusion) {
			if (!l.isGround())
				continue;
			if (l.isPositive()) {
				if (!factbase.contains(l))
					return true;
			} else {
				if (factbase.contains(l.clone().toggleNegation()))
					return true;
			}
		}
		return false;
	}

	public List<Map<VariableParam, LiteralParam>> getGroundingsUnderWhichALiteralAppearsInFactBase(
			Collection<Literal> factbase, Literal l, int maxSubstitutions) {

		List<VariableParam> openParams = l.getVariableParams();

		/*
		 * if there are no open params, we do not need to make decisions here, so just
		 * compute subsolutions
		 */
		logger.debug("Compute possible sub-groundings of the open parameters.");
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
				if (!fact.getPropertyName().equals(l.getPropertyName()) || fact.isPositive() != l.isPositive())
					continue;
				logger.trace("Considering known literal {} as a literal that can be used for grounding", fact);
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
				if (!paramsCanBeMatched)
					continue;
				logger.trace("Adding {} as a possible such grounding.", submap);
				choices.add(submap);
				if (choices.size() >= maxSubstitutions) {
					logger.debug("Reached maximum number {} of required substitutions. Returning what we have so far.",
							maxSubstitutions);
					return choices;
				}
			}
		}
		logger.debug("Done. Computation of {} groundings took {}ms", choices.size(),
				System.currentTimeMillis() - start);
		return choices;
	}

	public boolean verifyThatGroundingEnablesConclusion(Collection<Literal> factbase, Collection<Literal> conclusion,
			Map<VariableParam, LiteralParam> grounding) {
		for (Literal l : conclusion) {
			Literal lg = new Literal(l, grounding);
			if (factbase.contains(lg) != l.isPositive()) {
				System.err.println("Literal " + l + " in conclusion ground to " + lg + " does not follow from state: ");
				factbase.stream().sorted((l1, l2) -> l1.toString().compareTo(l2.toString()))
						.forEach(lit -> System.out.println("\t" + lit));
				return false;
			}
		}
		return true;
	}
}
