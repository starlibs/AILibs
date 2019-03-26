//package de.upb.crc901.mlplan.multiclasswithreduction;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//import java.util.Set;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import jaicore.basic.sets.SetUtil;
//import jaicore.basic.sets.SetUtil.Pair;
//import jaicore.logic.fol.structure.ConstantParam;
//import jaicore.logic.fol.structure.Literal;
//import jaicore.logic.fol.structure.Monom;
//import jaicore.logic.fol.structure.VariableParam;
//import jaicore.logic.fol.theories.set.SetTheoryUtil;
//import jaicore.planning.graphgenerators.task.ceociptfd.OracleTaskResolver;
//import jaicore.planning.model.ceoc.CEOCAction;
//import jaicore.planning.model.ceoc.CEOCOperation;
//import jaicore.planning.model.core.Action;
//import jaicore.planning.model.core.Operation;
//import jaicore.planning.model.task.ceocipstn.CEOCIPSTNPlanningProblem;
//import weka.core.Instances;
//
//public class RPNDOracleTaskSolver implements OracleTaskResolver {
//
//	private static final Logger logger = LoggerFactory.getLogger(RPNDOracleTaskSolver.class);
//	private final Random rand;
//	private final String classifierName;
//	private final Instances data;
//	private CEOCOperation configChildNodesOp;
//
//	public RPNDOracleTaskSolver(Random rand, String classifierName, Instances data, CEOCIPSTNPlanningProblem problem) {
//		super();
//		this.rand = rand;
//		this.classifierName = classifierName;
//		this.data = data;
//		for (Operation op : problem.getDomain().getOperations()) {
//			if (op.getName().equals("configChildNodes")) {
//				configChildNodesOp = (CEOCOperation) op;
//				break;
//			}
//		}
//		if (configChildNodesOp == null)
//			throw new IllegalArgumentException("Domain has no operation with name \"configChildNodes\"");
//	}
//
//	private interface Splitter {
//		Split split(Collection<String> set) throws Exception;
//	}
//
//	class Split extends Pair<Set<String>, Set<String>> {
//
//		public Split(Set<String> x, Set<String> y) {
//			super(x, y);
//		}
//
//		@Override
//		public String toString() {
//			return "Split [getX()=" + getX() + ", getY()=" + getY() + "]";
//		}
//	}
//
//	private class RPNDSplitter implements Splitter {
//
//		private final Instances data;
//
//		public RPNDSplitter(Instances data) {
//			super();
//			this.data = data;
//		}
//
//		@Override
//		public Split split(Collection<String> set) throws Exception {
//			ClassSplit<String> split = NestedDichotomyUtil.createGeneralRPNDBasedSplit(set, rand, classifierName, data);
//			return new Split(new HashSet<>(split.getL()), new HashSet<>(split.getR()));
//		}
//	}
//
////	private class RandomSplit implements Splitter {
////
////		@Override
////		public Split split(Collection<String> setOrig) {
////			List<String> set = new ArrayList<>(setOrig);
////			Collections.shuffle(set, rand);
////			int offset = rand.nextInt(set.size() - 1);
////
////			/* perform this specific split */
////			Set<String> c1 = new HashSet<>();
////			Set<String> c2 = new HashSet<>();
////			boolean offsetReached = false;
////			int i = 0;
////			for (String b : set) {
////				if (!offsetReached)
////					c1.add(b);
////				else
////					c2.add(b);
////				i++;
////				if (i == offset)
////					offsetReached = true;
////			}
////
////			return new Split(c1, c2);
////		}
////	}
//
////	private class GreedySplitter implements Splitter {
////
////		private final Instances data;
////
////		public GreedySplitter(Instances data) {
////			super();
////			this.data = data;
////		}
////
////		@Override
////		public Split split(Collection<String> set) {
////			// ClassSplit<String> split = NestedDichotomyUtil.createGreedySplit(set, rand, classifierName, data);
////			// return new Split(new HashSet<>(split.getL()), new HashSet<>(split.getR()));
////			return null;
////		}
////	}
//
//	@Override
//	public Collection<List<Action>> getSubSolutions(Monom state, Literal task) throws Exception {
//
//		/* prepare template grounding for actions */
//		String nameOfParent = task.getConstantParams().get(0).getName();
//		String nameOfLC = task.getConstantParams().get(1).getName();
//		String nameOfRC = task.getConstantParams().get(2).getName();
//		Map<VariableParam, ConstantParam> groundingTemplate = new HashMap<>();
//		groundingTemplate.put(new VariableParam("p"), new ConstantParam(nameOfParent));
//		groundingTemplate.put(new VariableParam("lc"), new ConstantParam(nameOfLC));
//		groundingTemplate.put(new VariableParam("rc"), new ConstantParam(nameOfRC));
//
//		List<String> set = new ArrayList<>(SetTheoryUtil.getObjectsInSet(state, nameOfParent));
//		logger.info("Compute RPND split for {}", set);
//
//		if (set.size() <= 1) {
//			// throw new UnsupportedOperationException("Cannot create successor where rest problem consists of one or less classes.");
//			return new ArrayList<>();
//		}
//
//		/* if no decision is to be made, return the single possible solution */
//		if (set.size() == 2) {
//
//			/* determine subsolutions */
//			Collection<List<Action>> subsolutions = new ArrayList<>();
//			Map<VariableParam, ConstantParam> grounding = new HashMap<>(groundingTemplate);
//			grounding.put(new VariableParam("ss"), new ConstantParam("{" + set.get(0) + "}"));
//			List<Action> subsolution = new ArrayList<>();
//			subsolution.add(new CEOCAction(configChildNodesOp, grounding));
//			subsolutions.add(subsolution);
//			return subsolutions;
//		}
//
//		List<Splitter> splitters = new ArrayList<>();
//		// int max = (int)Math.log(set.size());
//		int max = 1;
//		logger.info("Make {} suggestions for {} classes", max, set.size());
//		for (int i = 0; i < max; i++) {
//			splitters.add(new RPNDSplitter(data));
//			// splitters.add(new GreedySplitter(data));
//		}
//
//		/* determine subsolutions */
//		Collection<List<Action>> subsolutions = new ArrayList<>();
//		try {
//			for (Splitter splitter : splitters) {
//				logger.info("Compute next split");
//				Split split = splitter.split(set);
//				logger.info("Split computed: {}", split);
//				Map<VariableParam, ConstantParam> grounding = new HashMap<>(groundingTemplate);
//				grounding.put(new VariableParam("ss"), new ConstantParam(SetUtil.serializeAsSet(split.getX())));
//				List<Action> subsolution = new ArrayList<>();
//				subsolution.add(new CEOCAction(configChildNodesOp, grounding));
//				subsolutions.add(subsolution);
//			}
//		} catch (InterruptedException e) {
//
//		}
//
//		logger.info("Ready with RPND computation");
//		return subsolutions;
//	}
//
//}
