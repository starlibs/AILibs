package jaicore.planning.graphgenerators.task.ceoctfd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.sets.SetUtil;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.graphgenerators.task.tfd.TFDGraphGenerator;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.ceoc.CEOCAction;
import jaicore.planning.model.ceoc.CEOCOperation;
import jaicore.planning.model.core.Action;
import jaicore.planning.model.task.ceocstn.CEOCSTNPlanningProblem;
import jaicore.planning.model.task.ceocstn.OCMethod;
import jaicore.planning.model.task.stn.MethodInstance;

@SuppressWarnings("serial")
public class CEOCTFDGraphGenerator<O extends CEOCOperation, M extends OCMethod, A extends CEOCAction> extends TFDGraphGenerator<O, M, A> {
	
	private final static Logger logger = LoggerFactory.getLogger(CEOCTFDGraphGenerator.class);

	public CEOCTFDGraphGenerator(CEOCSTNPlanningProblem<O, M, A> problem) {
		super(problem);
	}

	@Override
	protected TFDNode postProcessPrimitiveTaskNode(TFDNode node) {
		Monom state = node.getState();
		state.getParameters().stream().filter(p -> p.getName().startsWith("newVar") && !state.contains(new Literal("def('" + p.getName() + "')")))
				.forEach(p -> state.add(new Literal("def('" + p.getName() + "')")));
		return node;
	}
	
	@Override
	public boolean isPathSemanticallySubsumed(List<TFDNode> path, List<TFDNode> compl) throws InterruptedException {
		if (compl.size() < path.size()) {
			logger.debug("Ignoring this partial path, because its completion is shorter than the path we already have.");
			return false;
		}
		if (path.equals(compl)) {
			logger.debug("Return true, because the paths are even equal.");
			return true;
		}

		Map<ConstantParam, ConstantParam> map = new HashMap<>();
		boolean allUnifiable = true;
		for (int i = 0; i < path.size(); i++) {
			TFDNode current = path.get(i);
			TFDNode partner = compl.get(i);

			/* check whether the chosen method or operation is the same */
			final Action a1 = current.getAppliedAction();
			final Action a2 = partner.getAppliedAction();
			if ((a1 == null) != (a2 == null)) {
				allUnifiable = false;
				logger.trace("Not unifiable because one node applies an action and the other not (either it applies nothing or a method instance).");
				break;
			}
			if (a1 != null && !a1.getOperation().equals(a2.getOperation())) {
				allUnifiable = false;
				logger.trace("Not unifiable because operations {} and {} of a1 and a2 respectively deviate", a1.getOperation(), a2.getOperation());
				break;
			}
			if (a1 == null) {
				final MethodInstance mi1 = current.getAppliedMethodInstance();
				final MethodInstance mi2 = partner.getAppliedMethodInstance();

				/* the nodes just don't do anything (should be the root) */
				if (mi1 == null && mi2 == null) {
					continue;
				}

				if ((mi1 == null) != (mi2 == null)) {
					allUnifiable = false;
					logger.trace("Not unifiable because one node applies a method instance and the other not (either an action or nothing)");
					break;
				}
				if (!mi1.getMethod().equals(mi2.getMethod())) {
					allUnifiable = false;
					logger.trace("Not unifiable because methods {} and {} of m1 and m2 respectively deviate", mi1.getMethod(), mi2.getMethod());
					break;
				}
			}

			/* compute substitutions of new vars */
			Collection<ConstantParam> varsInCurrent = new HashSet<>(current.getState().getConstantParams());
			for (Literal l : current.getRemainingTasks())
				varsInCurrent.addAll(l.getConstantParams());
			Collection<ConstantParam> varsInPartner = new HashSet<>(partner.getState().getConstantParams());
			for (Literal l : partner.getRemainingTasks())
				varsInPartner.addAll(l.getConstantParams());
			Collection<ConstantParam> unboundVars = SetUtil.difference(varsInCurrent, map.keySet());
			Collection<ConstantParam> possibleTargets = SetUtil.difference(varsInPartner, map.values());
			for (ConstantParam p : new ArrayList<>(unboundVars)) {
				if (possibleTargets.contains(p)) {
					map.put(p, p);
					unboundVars.remove(p);
					possibleTargets.remove(p);
				}
			}

			/* if the relation between vars in the nodes is completely known, we can easily decide whether they are unifiable */
			if (unboundVars.isEmpty()) {
				if (getRenamedState(current.getState(), map).equals(partner.getState())
						&& getRenamedRemainingList(current.getRemainingTasks(), map).equals(partner.getRemainingTasks()))
					continue;
				else {
					allUnifiable = false;
					break;
				}
			}

			/* otherwise, we must check possible mappings between the still unbound vars */
			boolean unified = false;
			Collection<Map<ConstantParam, ConstantParam>> possibleMappingCompletions = SetUtil.allMappings(unboundVars, possibleTargets, true, true, true);
			for (Map<ConstantParam, ConstantParam> mappingCompletion : possibleMappingCompletions) {

				/* first check whether the state is equal */
				Monom copy = getRenamedState(current.getState(), mappingCompletion);
				if (!copy.equals(partner.getState()))
					continue;

				/* if this is the case, check whether the remaining tasks are equal */
				List<Literal> copyOfTasks = getRenamedRemainingList(current.getRemainingTasks(), mappingCompletion);
				if (!copyOfTasks.equals(partner.getRemainingTasks()))
					continue;

				/* now we know that this node can be unified. We add the respective map and quit the current node pair */
				map.putAll(mappingCompletion);
				unified = true;
				break;

			}
			if (!unified) {
				allUnifiable = false;
				break;
			}
		}

		/* if all nodes were unifiable, return this path */
		if (allUnifiable) {
			logger.info("Returning true, because this path is unifiable with the given one.");
			return true;
		}
		else
			return false;
	}
	

	
	private Monom getRenamedState(Monom state, Map<ConstantParam, ConstantParam> map) {
		Monom copy = new Monom(state, map);
		return copy;
	}

	private List<Literal> getRenamedRemainingList(List<Literal> remainingList, Map<ConstantParam, ConstantParam> map) {
		List<Literal> copyOfTasks = new ArrayList<>();
		for (Literal l : remainingList) {
			copyOfTasks.add(new Literal(l, map));
		}
		return copyOfTasks;
	}
}
