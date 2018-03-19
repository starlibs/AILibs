package de.upb.crc901.mlplan.core;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.upb.crc901.configurationsetting.compositiondomain.KnowledgeModule;
import de.upb.crc901.configurationsetting.operation.ConfigurationState;
import de.upb.crc901.configurationsetting.serialization.SerializationWrapper;
import de.upb.crc901.configurationsetting.serialization.testset.TestsetParser;
import jaicore.basic.SetUtil;
import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.ml.WekaUtil;
import jaicore.planning.model.ceoc.CEOCOperation;
import jaicore.planning.model.task.ceocipstn.CEOCIPSTNPlanningDomain;
import jaicore.planning.model.task.ceocipstn.CEOCIPSTNPlanningProblem;
import jaicore.planning.model.task.ceocipstn.OCIPMethod;
import jaicore.planning.model.task.stn.TaskNetwork;
import weka.core.Instances;

public class TaskProblemGenerator {

	public CEOCIPSTNPlanningProblem getProblem(File testsetFile, Instances dataset) {

		try {
			/* read testset */
			TestsetParser parser = new TestsetParser();
			SerializationWrapper wrapper = parser.parse(new FileReader(testsetFile));

			/* create operations */
			List<CEOCOperation> operations = wrapper.getOperationRepository().stream().map(o -> operationFromCRCSettingToJAICore(o)).collect(Collectors.toList());

			/* create methods */
			List<OCIPMethod> methods = new ArrayList<>(wrapper.getMethods());

			/* setup initial state and task network */
			CEOCOperation query = operationFromCRCSettingToJAICore(wrapper.getChallengeSet().getFirst());
			Map<VariableParam, ConstantParam> grounding = new HashMap<>();
			for (VariableParam p : query.getParams()) {
				grounding.put(p, new ConstantParam(p.getName()));
			}
			Monom init = new Monom(query.getPrecondition(), grounding);

			/* now we implicitly assume that there is a parameter c, and c contains all the classes. */
			if (dataset != null) {
				init.add(new Literal("definesClassesOf('c','x')"));
				for (String className : WekaUtil.getClassesDeclaredInDataset(dataset)) {
					init.add(new Literal("in('" + className + "','c')"));
				}
			}

			/* KnowledgeModule is simply an extension of a set of clauses */
			KnowledgeModule knowledge = wrapper.getCompositionDomain().getKnowledgeModule();
			CNFFormula convertedKnowledge = new CNFFormula(knowledge);

			TaskNetwork network = new TaskNetwork();
			network.addItem(new Literal(query.getAddLists().get(new CNFFormula()).iterator().next(), grounding));

			/* create and return the planning problem */
			return new CEOCIPSTNPlanningProblem(new CEOCIPSTNPlanningDomain(operations, methods), convertedKnowledge, init, network);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public CEOCOperation operationFromCRCSettingToJAICore(de.upb.crc901.configurationsetting.operation.Operation o) {
		Map<CNFFormula, Monom> addLists = new HashMap<>();
		addLists.put(new CNFFormula(), new Monom(o.getEffect().getCondition().stream().filter(l -> l.isPositive()).map(l -> convertLiteral(l)).collect(Collectors.toList())));
		Map<CNFFormula, Monom> deleteLists = new HashMap<>();
		deleteLists.put(new CNFFormula(),
				new Monom(o.getEffect().getCondition().stream().filter(l -> l.isNegated()).map(l -> convertLiteral(l).toggleNegation()).collect(Collectors.toList())));
		List<VariableParam> parameters = new ArrayList<>();
		parameters.addAll(convertVarParamList(o.getInputParameters()));
		parameters.addAll(convertVarParamList(o.getOutputParameters()));
		return new CEOCOperation(o.getName(), parameters, new Monom(o.getPrecondition().getCondition().stream().map(l -> convertLiteral(l)).collect(Collectors.toList())), addLists,
				deleteLists, convertVarParamList(o.getOutputParameters()));
	}

	public de.upb.crc901.configurationsetting.operation.Operation operationFromJAICoreToCRCSetting(CEOCOperation o) {
		ConfigurationState pre = new ConfigurationState();
		pre.addAll(o.getPrecondition());
		ConfigurationState post = new ConfigurationState();
		post.addAll(o.getAddLists().values().iterator().next());
		return new de.upb.crc901.configurationsetting.operation.Operation(o.getName(), new ArrayList<>(SetUtil.difference(o.getParams(), o.getOutputs())), o.getOutputs(), pre,
				post, null);
	}

	private Literal convertLiteral(jaicore.logic.fol.structure.Literal l) {
		return new Literal(l.getPropertyName(), convertLiteralParamList(l.getParameters()), l.isPositive());
	}

	private List<LiteralParam> convertLiteralParamList(Collection<? extends jaicore.logic.fol.structure.LiteralParam> l) {
		return l.stream().map(p -> convertLiteralParam(p)).collect(Collectors.toList());
	}

	private LiteralParam convertLiteralParam(jaicore.logic.fol.structure.LiteralParam p) {
		return (p instanceof jaicore.logic.fol.structure.VariableParam) ? new VariableParam(p.getName(), p.getType()) : new ConstantParam(p.getName(), p.getType());
	}

	private List<VariableParam> convertVarParamList(Collection<? extends jaicore.logic.fol.structure.VariableParam> l) {
		return l.stream().map(p -> convertVariableParam(p)).collect(Collectors.toList());
	}

	private VariableParam convertVariableParam(jaicore.logic.fol.structure.VariableParam p) {
		return new VariableParam(p.getName(), p.getType());
	}
}
