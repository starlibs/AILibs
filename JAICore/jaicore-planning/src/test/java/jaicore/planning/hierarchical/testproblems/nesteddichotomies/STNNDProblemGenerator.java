package jaicore.planning.hierarchical.testproblems.nesteddichotomies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.planning.classical.problems.ce.CEOperation;
import jaicore.planning.classical.problems.ceoc.CEOCOperation;
import jaicore.planning.hierarchical.problems.ceocstn.CEOCSTNPlanningProblem;
import jaicore.planning.hierarchical.problems.ceocstn.OCMethod;
import jaicore.planning.hierarchical.problems.stn.Method;
import jaicore.planning.hierarchical.problems.stn.STNPlanningDomain;
import jaicore.planning.hierarchical.problems.stn.STNPlanningProblem;
import jaicore.planning.hierarchical.problems.stn.TaskNetwork;

public class STNNDProblemGenerator {

	public static STNPlanningProblem getNestedDichotomyCreationProblem(String rootClusterName, List<String> classes) {
		CEOCSTNPlanningProblem problem = jaicore.planning.hierarchical.testproblems.nesteddichotomies.CEOCSTNNDProblemGenerator.getNestedDichotomyCreationProblem(rootClusterName, classes, true, 1, 1);
		
		List<CEOperation> operations = new ArrayList<>();
		for (CEOCOperation op : problem.getDomain().getOperations()) {
			Monom precondition = op.getPrecondition();
			Map<CNFFormula,Monom> addLists = op.getAddLists();
			Map<CNFFormula,Monom> deleteLists = op.getDeleteLists();
			List<VariableParam> parameters = new ArrayList<>(op.getParams());
			if (!op.getOutputs().isEmpty()) {
				for (VariableParam out : op.getOutputs()) {
					precondition.addAll(new Monom("cluster(" + out.getName() + ") & !inuse(" + out.getName() + ")"));
					addLists.get(new CNFFormula()).add(new Literal("inuse(" + out.getName() + ")"));
				}
				parameters.add(new VariableParam("next"));
				addLists.get(new CNFFormula()).add(new Literal("active(next)"));
				deleteLists.put(new CNFFormula(), new Monom("active(" + op.getOutputs().get(0).getName() + ")"));
				precondition.addAll(new Monom("active(" + op.getOutputs().get(0).getName() + ") & succ(" + op.getOutputs().get(0).getName() + ", " + op.getOutputs().get(1).getName() + ") & succ(" + op.getOutputs().get(1).getName() + ", next)"));
			}
			operations.add(new CEOperation(op.getName(), parameters, precondition, addLists, deleteLists));
		}
		List<Method> methods = new ArrayList<>();
		for (OCMethod method : problem.getDomain().getMethods()) {
			List<VariableParam> parameters = new ArrayList<>(method.getParameters());
			Monom precondition = method.getPrecondition();
			TaskNetwork nw = method.getNetwork();
			if (!method.getOutputs().isEmpty()) {
				for (VariableParam out : method.getOutputs()) {
					precondition.addAll(new Monom("cluster(" + out.getName() + ") & !inuse(" + out.getName() + ")"));
				}
				parameters.add(new VariableParam("next"));
				List<Literal> tasks = new ArrayList<>(nw.getItems()); 
				for (Literal task : tasks) {
					if (task.getPropertyName().endsWith("initChildClusters")) {
						List<LiteralParam> taskParams = new ArrayList<>(task.getParameters());
						taskParams.add(new VariableParam("next"));
						Collection<Literal> succ = nw.getSuccessors(task);
						Collection<Literal> pred = nw.getPredecessors(task);
						nw.removeItem(task);
						Literal newTask = new Literal(task.getPropertyName(), taskParams);
						nw.addItem(newTask);
						for (Literal l : succ)
							nw.addEdge(newTask, l);
						for (Literal l : pred)
							nw.addEdge(l, newTask);
					}
				}
				precondition.addAll(new Monom("active(" + method.getOutputs().get(0).getName() + ") & succ(" + method.getOutputs().get(0).getName() + ", " + method.getOutputs().get(1).getName() + ") & succ(" + method.getOutputs().get(1).getName() + ", next)"));
			}
			System.out.println(precondition);
			methods.add(new Method(method.getName(), parameters, method.getTask(), precondition, nw, method.isLonely()));
		}
		
		STNPlanningDomain domain = new STNPlanningDomain(operations, methods);
		Monom init = problem.getInit();
		init.add(new Literal("inuse('root')"));
		for (int i = 0; i < 2 * classes.size(); i++) {
			init.addAll(i == 0 ? new Monom("cluster('c0') & active('c1')") : new Monom("cluster('c" + i + "') & succ('c" + (i-1) + "','c" + i + "')"));
		}
		System.out.println(init);
		STNPlanningProblem newProblem = new STNPlanningProblem(domain, null, init, problem.getNetwork());
		return newProblem;
	}
}
