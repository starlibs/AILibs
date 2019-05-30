package hasco.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import hasco.core.Util;
import hasco.model.CategoricalParameterDomain;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.Parameter;
import hasco.model.IParameterDomain;
import hasco.serialization.ComponentLoader;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;

public class UtilTester {

	@Test
	public void testComponentInstanceBuilder() throws Exception {
		Monom state = new Monom("iface(R) & 	iface(WekaBaseClassifier) & 	def(newVar7)& 	def(newVar3)& 	parameterFocus(newVar3, A, -1.0) & 	iface(BaseClassifier) & 	component(request) & 	iface(weka.attributeSelection.InfoGainAttributeEval) & 	def(newVar12) & 	parameterContainer(weka.attributeSelection.PrincipalComponents, R, newVar3, newVar10) & 	resolves(newVar1, evaluator, weka.attributeSelection.PrincipalComponents, newVar3) & 	component(newVar4) & 	overwritten(newVar8) & 	iface(weka.attributeSelection.SymmetricalUncertAttributeEval) & 	iface(evaluator) & 	resolves(solution, AbstractPreprocessor, weka.attributeSelection.AttributeSelection, newVar1) & 	parameterContainer(weka.attributeSelection.AttributeSelection, M, newVar1, newVar5) & 	iface(weka.attributeSelection.ReliefFAttributeEval) & 	iface(weka.classifiers.functions.Logistic) & 	iface(weka.attributeSelection.Ranker) & 	iface(searcher) & 	interfaceIdentifier(weka.attributeSelection.AttributeSelection, search, newVar1, newVar4) & 	iface(weka.classifiers.bayes.NaiveBayesMultinomial) & 	def(newVar8) & 	parameterContainer(weka.attributeSelection.PrincipalComponents, A, newVar3, newVar9) & 	parameterFocus(newVar3, R, 0.95) & 	def(newVar4) & 	parameterContainer(weka.attributeSelection.PrincipalComponents, C, newVar3, newVar7) & 	iface(M) & 	iface(weka.attributeSelection.CorrelationAttributeEval) & 	component(solution) & 	val(newVar6, 1) & 	iface(weka.classifiers.bayes.NaiveBayes) & 	val(newVar8, true) & 	overwritten(newVar7) & 	iface(D) & 	component(newVar3) & 	resolves(newVar1, searcher, weka.attributeSelection.Ranker, newVar4) & 	val(newVar11, false) & 	iface(H) & 	parameterContainer(weka.attributeSelection.PrincipalComponents, O, newVar3, newVar8) & 	resolves(solution, BaseClassifier, weka.classifiers.bayes.NaiveBayes, newVar2) & 	def(newVar9) & 	parameterContainer(weka.classifiers.bayes.NaiveBayes, K, newVar2, newVar12) & 	interfaceIdentifier(pipeline, preprocessor, solution, newVar1) & 	def(newVar5) & 	iface(weka.attributeSelection.GreedyStepwise) & 	iface(MLPipeline) & 	val(newVar12, true) & 	def(newVar1) & 	parameterContainer(weka.attributeSelection.PrincipalComponents, numActivator, newVar3, newVar6) & 	iface(C) & 	iface(AbstractClassifier) & 	iface(Test) & 	overwritten(newVar6) & 	iface(weka.attributeSelection.PrincipalComponents) & 	iface(weka.attributeSelection.AttributeSelection) & 	def(newVar10) & 	component(newVar2) & 	overwritten(newVar12) & 	iface(pipeline) & 	iface(weka.attributeSelection.CfsSubsetEval) & 	iface(S) & 	interfaceIdentifier(pipeline, classifier, solution, newVar2) & 	iface(AbstractPreprocessor) & 	val(newVar7, true) & 	iface(weka.attributeSelection.GainRatioAttributeEval) & 	val(newVar5, true) & 	def(newVar6) & 	iface(weka.classifiers.bayes.BayesNet) & 	def(newVar2) & 	iface(B) & 	resolves(request, AbstractClassifier, pipeline, solution) & 	def(newVar11) & 	overwritten(newVar9) & 	component(newVar1) & 	iface(weka.attributeSelection.BestFirst) & 	overwritten(newVar11) & 	interfaceIdentifier(weka.attributeSelection.AttributeSelection, eval, newVar1, newVar3) & 	iface(weka.attributeSelection.OneRAttributeEval) & 	parameterContainer(weka.classifiers.bayes.NaiveBayes, D, newVar2, newVar11)");
		List<ConstantParam> params = new ArrayList<>();
		params.add(new ConstantParam("newVar9"));
		params.add(new ConstantParam("[125.36470588235294, 253.74117647058824]"));
		state.add(new Literal("val", params));
		params.clear();
		params.add(new ConstantParam("newVar10"));
		params.add(new ConstantParam("[0.5,1.0]"));
		state.add(new Literal("val", params));
		ComponentInstance instance = Util.getComponentInstanceFromState(new ComponentLoader(new File("testrsc/weka/weka-all-autoweka.json")).getComponents(), state, "solution", false);
		assertEquals("[125.36470588235294, 253.74117647058824]", instance.getSatisfactionOfRequiredInterfaces().get("preprocessor").getSatisfactionOfRequiredInterfaces().get("eval").getParameterValue("A"));
		instance = Util.getComponentInstanceFromState(new ComponentLoader(new File("testrsc/weka/weka-all-autoweka.json")).getComponents(), state, "solution", true);
		assertEquals("190", instance.getSatisfactionOfRequiredInterfaces().get("preprocessor").getSatisfactionOfRequiredInterfaces().get("eval").getParameterValue("A"));
	}
	
	@Test
	public void testParameterDomainUpdates() throws Exception {
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem = new RefinementConfiguredSoftwareConfigurationProblem<>(new File("testrsc/problemwithdependencies.json"), "IFace", n -> 0.0);
		Component bComponent = problem.getComponents().stream().filter(c -> c.getName().equals("B")).findFirst().get();
		Parameter dParameter = bComponent.getParameterWithName("d");

		/* first check that the domain is default if c is not set at all */
		{
			ComponentInstance inst = new ComponentInstance(bComponent, null, null);
			Map<Parameter, IParameterDomain> newDomains = Util.getUpdatedDomainsOfComponentParameters(inst);
			assertEquals(dParameter.getDefaultDomain(), newDomains.get(dParameter));
		}
		
		/* now check that the domain remains default if c is explicitly set to false */
		{
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("c", "false");
			ComponentInstance inst = new ComponentInstance(bComponent, parameterValues, null);
			Map<Parameter, IParameterDomain> newDomains = Util.getUpdatedDomainsOfComponentParameters(inst);
			assertEquals(dParameter.getDefaultDomain(), newDomains.get(dParameter));
		}
		
		/* now check that the domain is changed in the intended manner when c is true */
		{
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("c", "true");
			ComponentInstance inst = new ComponentInstance(bComponent, parameterValues, null);
			Map<Parameter, IParameterDomain> newDomains = Util.getUpdatedDomainsOfComponentParameters(inst);
			Set<String> expectedValues = new HashSet<>();
			expectedValues.add("blue");
			expectedValues.add("white");
			expectedValues.add("red");
			expectedValues.add("green");
			expectedValues.add("black");
			IParameterDomain expectedDomain = new CategoricalParameterDomain(expectedValues);
			assertEquals(expectedDomain, newDomains.get(dParameter));
		}
	}
}
