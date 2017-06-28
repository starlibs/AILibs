package util.planning.model.task.ceocstn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import util.logic.CNFFormula;
import util.logic.Clause;
import util.logic.Literal;
import util.logic.Monom;
import util.logic.VariableParam;
import util.planning.model.ceoc.CEOCOperation;
import util.planning.model.task.stn.TaskNetwork;

public class StandardProblemFactory {
	
	public static CEOCSTNPlanningProblem getNestedDichotomyCreationProblem(String rootClusterName, Collection<String> classesInit) {
		
		/* define operations */
		List<String> classes = classesInit.stream().sorted().collect(Collectors.toList());
		if (!classes.equals(classesInit)) {
			System.out.println("Reordered classes!");
//			System.out.println(classesInit);
//			System.out.println(classes);
		}
		List<CEOCOperation> operations = new ArrayList<>();
		Map<CNFFormula,Monom> addLists = new HashMap<>(), deleteLists = new HashMap<>();
		
		/* operations to init child clusters and to close them */
		for (String c : classes) {
			addLists.put(new CNFFormula(new Monom("in('" + c + "',p)")), new Monom("in('" + c + "', lc)"));
			addLists.put(new CNFFormula(new Monom("smallest('" + c + "',p)")), new Monom("smallest('" + c + "', lc)"));
		}
		operations.add(new CEOCOperation("initChildClusters", Arrays.asList(new VariableParam[] { new VariableParam("p"), new VariableParam("lc"), new VariableParam("rc") }), new Monom(), addLists, new HashMap<>(), Arrays.asList(new VariableParam[] { new VariableParam("lc"), new VariableParam("rc") })));
		operations.add(new CEOCOperation("closeClusters", Arrays.asList(new VariableParam[] { new VariableParam("l"), new VariableParam("lw"), new VariableParam("r"), new VariableParam("rw") }), new Monom("in(lw,l) & in(rw,r)"), new HashMap<>(), new HashMap<>(), new ArrayList<>()));
		
		/* operation to shift the first element from a left cluster to a right one */
		addLists = new HashMap<>();
		addLists.put(new CNFFormula(), new Monom("in(x,r) & biggest(x,r) & smallest(x,r)"));
			
		/* updates the smallest of */
		Monom case1 = new Monom("smallest(x,l)");
		for (String c2ndSmallest : classes) {
			CNFFormula allQuantifiedBiggerRelation = new CNFFormula(case1);
			allQuantifiedBiggerRelation.add(new Clause("in('" + c2ndSmallest + "', l)"));
			for (String cOther : classes) {
				if (!cOther.equals(c2ndSmallest)) {
					allQuantifiedBiggerRelation.add(new Clause("!in('" + cOther + "', l) | x = '" + cOther + "' | bigger('" + cOther + "', '" + c2ndSmallest + "')"));
				}
			}
			addLists.put(allQuantifiedBiggerRelation, new Monom("smallest('" + c2ndSmallest + "', l)"));
		}
			
			/* case two: some other is smallest */
//			Monom case2 = new Monom("smallest('" + cSmallest + "',p) & '" + cSmallest + "' != x");
//			addLists.put(new CNFFormula(case2), new Monom("smallest('" + cSmallest + "', lc)"));
		deleteLists.put(new CNFFormula(), new Monom("in(x,l) & smallest(x,l)"));
		operations.add(new CEOCOperation("shiftFirstElement", Arrays.asList(new VariableParam[] { new VariableParam("x"), new VariableParam("l"), new VariableParam("r") }), new Monom("in(x,l)"), addLists, deleteLists, new ArrayList<>()));
		
		/* operation to shift a new biggest element from a left cluster to a right one */
		addLists = new HashMap<>();
		addLists.put(new CNFFormula(), new Monom("in(x,r) & biggest(x,r)"));
		deleteLists = new HashMap<>();
		deleteLists.put(new CNFFormula(), new Monom("in(x,l) & biggest(y,r)"));
		operations.add(new CEOCOperation("shiftElement", Arrays.asList(new VariableParam[] { new VariableParam("x"), new VariableParam("y"), new VariableParam("l"), new VariableParam("r") }), new Monom("in(x,l) & biggest(y,r)"), addLists, deleteLists, new ArrayList<>()));
		
		/* add primitive action to select a representative of a cluster */
		addLists = new HashMap<>();
		addLists.put(new CNFFormula(), new Monom("represents(x,c)"));
		deleteLists = new HashMap<>();
		deleteLists.put(new CNFFormula(), new Monom("in(x,c) & smallest(x,c) & biggest(x,c)"));
		operations.add(new CEOCOperation("declareClusterRepresentant", Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("x") }), new Monom("in(x,c)"), addLists, deleteLists, new ArrayList<>()));
		
		/* add primitive action to assert emptyness of a cluster */
		Monom precondition = new Monom();
		for (String c : classes) {
			precondition.add(new Literal("!in('" + c + "', c)"));
		}
		addLists = new HashMap<>();
		addLists.put(new CNFFormula(), new Monom("empty(c)"));
		operations.add(new CEOCOperation("assertEmptyness", Arrays.asList(new VariableParam[] { new VariableParam("c") }), precondition, addLists, new HashMap<>(), new ArrayList<>()));
		
		/* define STN methods for the domain */
		Literal taskRefine = new Literal("refine(c)");
		Literal taskConfigure = new Literal("configureClusters(l,r)");
		Literal taskShiftFirst = new Literal("selectAndShiftFirstElement(l,r)");
		Literal taskShiftFirstNonSmallest = new Literal("selectAndShiftFirstNonSmallestElement(l,r)");
		Literal taskShiftFurther = new Literal("selectAndShiftFurtherElement(l,r)");
		Literal taskShiftAny = new Literal("shiftAnyElement(l,r)");
		Literal taskCheckLeaf = new Literal("checkLeaf(c)");
		
		List<OCMethod> methods = new ArrayList<>();
		methods.add(new OCMethod("assertbinarycluster",  Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("lc"), new VariableParam("rc") }), taskRefine, new Monom(), new TaskNetwork("initChildClusters(c,lc,rc) -> shiftAnyElement(lc,rc) -> checkLeaf(lc) -> checkLeaf(rc)"), true, Arrays.asList(new VariableParam[]{ new VariableParam("lc"), new VariableParam("rc")})));
		methods.add(new OCMethod("refineandclosetheotherunarily",  Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("lc"), new VariableParam("rc") }), taskRefine, new Monom(), new TaskNetwork("initChildClusters(c,lc,rc) -> selectAndShiftFirstElement(lc,rc) -> refine(lc)"), false, Arrays.asList(new VariableParam[]{ new VariableParam("lc"), new VariableParam("rc")})));
		methods.add(new OCMethod("refinebothwithatleasttwoonbothsides",  Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("lc"), new VariableParam("rc") }), taskRefine, new Monom(), new TaskNetwork("initChildClusters(c,lc,rc) -> selectAndShiftFirstNonSmallestElement(lc,rc) -> selectAndShiftFurtherElement(lc,rc) -> configureClusters(lc,rc) -> refine(lc) -> refine(rc)"), false, Arrays.asList(new VariableParam[]{ new VariableParam("lc"), new VariableParam("rc")})));
		methods.add(new OCMethod("shiftElementAndConfigure",  Arrays.asList(new VariableParam[] { new VariableParam("l"), new VariableParam("r") }), taskConfigure, new Monom(), new TaskNetwork("selectAndShiftFurtherElement(l,r) -> configureClusters(l,r)"), false, new ArrayList<>()));
		methods.add(new OCMethod("selectAndShiftFirstElementMethod",  Arrays.asList(new VariableParam[] { new VariableParam("x"), new VariableParam("l"), new VariableParam("r") }), taskShiftFirst, new Monom("in(x,l)"), new TaskNetwork("shiftFirstElement(x,l,r)"), false, new ArrayList<>()));
		methods.add(new OCMethod("selectAndShiftFirstNonSmallestElementMethod",  Arrays.asList(new VariableParam[] { new VariableParam("x"), new VariableParam("l"), new VariableParam("r") }), taskShiftFirstNonSmallest, new Monom("in(x,l) & !smallest(x,l)"), new TaskNetwork("shiftFirstElement(x,l,r)"), false, new ArrayList<>()));
		methods.add(new OCMethod("selectAndShiftFurtherElementMethod",  Arrays.asList(new VariableParam[] { new VariableParam("x"), new VariableParam("y"), new VariableParam("l"), new VariableParam("r") }), taskShiftFurther, new Monom("in(x,l) & biggest(y,r) & bigger(x,y)"), new TaskNetwork("shiftElement(x,y,l,r)"), false, new ArrayList<>()));
		methods.add(new OCMethod("selectAndShiftAnyElementMethod",  Arrays.asList(new VariableParam[] { new VariableParam("x"), new VariableParam("l"), new VariableParam("r") }), taskShiftAny, new Monom("in(x,l)  & !smallest(x,l) "), new TaskNetwork("shiftFirstElement(x,l,r)"), true, new ArrayList<>()));
		methods.add(new OCMethod("closeSetup",  Arrays.asList(new VariableParam[] { new VariableParam("l"), new VariableParam("lw"), new VariableParam("r"), new VariableParam("rw") }), taskConfigure, new Monom("in(lw,l) & in(rw,r)"), new TaskNetwork("closeClusters(l,lw,r,rw)"), true, new ArrayList<>()));
		methods.add(new OCMethod("checkLeafNode",  Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("x") }), taskCheckLeaf, new Monom("in(x,c)"), new TaskNetwork("declareClusterRepresentant(c,x) -> assertEmptyness(c)"), true, new ArrayList<>()));
		
		/* create STN domain */
		CEOCSTNPlanningDomain domain = new CEOCSTNPlanningDomain(operations, methods);
		
		Monom init = new Monom();
		for (String c : classes) {
//			init.add(new Literal("class('" + c + "')"));
			init.add(new Literal("in('" + c + "','" + rootClusterName + "')"));
		}
		init.add(new Literal("smallest('" + classes.get(0) + "', '" + rootClusterName + "')"));
		
		for (int i = 0; i < classes.size(); i++) {
			for (int j = i + 1; j < classes.size(); j++) {
				init.add(new Literal("bigger('" + classes.get(j) + "', '" + classes.get(i) + "')"));
			}
		}
		TaskNetwork network = new TaskNetwork("refine('" + rootClusterName + "')");
		return new CEOCSTNPlanningProblem(domain, null, init, network);
	}
}
