package de.upb.crc901.mlplan.metamining.pipelinecharacterizing;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Represents the connection to the data minining optimization ontology (DMOP)
 * enriched by the implementations of algorithms by WEKA. Thus, an object of
 * this class can be queried for WEKA classifiers as well as instances of
 * ASSearch and ASEvaluation.
 * 
 * @author Helena Graf
 *
 */
public class WEKAOntologyConnector implements IOntologyConnector {

	private static final List<String> classifierPortfolio = Arrays.asList("weka.classifiers.bayes.BayesNet",
			"weka.classifiers.bayes.NaiveBayes", "weka.classifiers.bayes.NaiveBayesMultinomial",
			"weka.classifiers.functions.Logistic", "weka.classifiers.functions.MultilayerPerceptron",
			"weka.classifiers.functions.SGD", "weka.classifiers.functions.SimpleLogistic",
			"weka.classifiers.functions.SMO", "weka.classifiers.functions.VotedPerceptron", "weka.classifiers.lazy.IBk",
			"weka.classifiers.lazy.KStar", "weka.classifiers.rules.DecisionTable", "weka.classifiers.rules.JRip",
			"weka.classifiers.rules.OneR", "weka.classifiers.rules.PART", "weka.classifiers.rules.ZeroR",
			"weka.classifiers.trees.DecisionStump", "weka.classifiers.trees.J48", "weka.classifiers.trees.LMT",
			"weka.classifiers.trees.RandomForest", "weka.classifiers.trees.RandomTree",
			"weka.classifiers.trees.REPTree");

	private static final List<String> evaluatorPortfolio = Arrays.asList("weka.attributeSelection.CfsSubsetEval",
			"weka.attributeSelection.CorrelationAttributeEval", "weka.attributeSelection.GainRatioAttributeEval",
			"weka.attributeSelection.InfoGainAttributeEval", "weka.attributeSelection.OneRAttributeEval",
			"weka.attributeSelection.PrincipalComponents", "weka.attributeSelection.ReliefFAttributeEval",
			"weka.attributeSelection.SymmetricalUncertAttributeEval");

	private static final List<String> searcherPortfolio = Arrays.asList("weka.attributeSelection.BestFirst",
			"weka.attributeSelection.GreedyStepwise", "weka.attributeSelection.Ranker");

	private static final String ontologyFileName = "DMOP_modified.owl";
	private static final String ontologyIRI = "http://www.e-lico.eu/ontologies/dmo/DMOP/DMOP.owl";
	private static final String ontologyIRISeparator = "#";

	private String classifierTopNode = "ModelingAlgorithm";
	private String searcherTopNode = "SearchStrategy";
	private String evaluatorTopNode = "DataProcessingAlgorithm";

	private OWLDataFactory dataFactory;
	private OWLOntology ontology;
	private boolean includeEqualSuperClasses = true;

	/**
	 * Creates an ontology connector using the standard ontology.
	 * 
	 * @throws OWLOntologyCreationException
	 *             If the ontology cannot be created
	 */
	public WEKAOntologyConnector() throws OWLOntologyCreationException {
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		dataFactory = ontologyManager.getOWLDataFactory();
		InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(ontologyFileName);
		ontology = ontologyManager.loadOntologyFromOntologyDocument(inputStream);
	}

	@Override
	public List<String> getAncestorsOfClassifier(String classifierName) {
		if (!classifierPortfolio.contains(classifierName)) {
			StringBuilder builder = new StringBuilder();
			builder.append(classifierName);
			builder.append(" is not supported by the used ontology.");
			throw new IllegalArgumentException(builder.toString());
		}
		return getAncestorsOfAlgorithmUntil(classifierName, classifierTopNode);
	}

	@Override
	public List<String> getAncestorsOfSearcher(String searcher) {
		if (!searcherPortfolio.contains(searcher)) {
			StringBuilder builder = new StringBuilder();
			builder.append(searcher);
			builder.append(" is not supported by the used ontology.");
			throw new IllegalArgumentException(builder.toString());
		}
		return getAncestorsOfAlgorithmUntil(searcher, searcherTopNode);
	}

	@Override
	public List<String> getAncestorsOfEvaluator(String evaluator) {
		if (!evaluatorPortfolio.contains(evaluator)) {
			StringBuilder builder = new StringBuilder();
			builder.append(evaluator);
			builder.append(" is not supported by the used ontology.");
			throw new IllegalArgumentException(builder.toString());
		}
		return getAncestorsOfAlgorithmUntil(evaluator, evaluatorTopNode);
	}

	/**
	 * Get the list of ancestors from most general to most specific concept up until
	 * the specified concept including the specified child and highest concept.
	 * 
	 * @param algorithm
	 *            The child algorithm
	 * @param until
	 *            The highest ancestor
	 * @return The ancestors of the child algorithm from the highest ancestor to the
	 *         child algorithm itself
	 */
	protected List<String> getAncestorsOfAlgorithmUntil(String algorithm, String until) {
		// Get the individual represented by the algorithm
		OWLNamedIndividual algorithmAsIndividual = dataFactory.getOWLNamedIndividual(getAsOntologyElement(algorithm));
		OWLClass algorithmClass = ontology.classAssertionAxioms(algorithmAsIndividual).findFirst().get()
				.getClassExpression().asOWLClass();

		// Get ancestors
		ArrayList<OWLClass> ancestors = new ArrayList<OWLClass>();
		ancestors.add(algorithmClass);
		for (int i = 0; i < ancestors.size(); i++) {
			int previousAncestorSize = ancestors.size();
			ontology.subClassAxiomsForSubClass(ancestors.get(i))
					.filter(axiom -> axiom.getSuperClass().getClassExpressionType() == ClassExpressionType.OWL_CLASS)
					.forEach(axiom -> {
						OWLClass toAdd = axiom.getSuperClass().asOWLClass();
						ancestors.add(toAdd);
					});
			// If we have not added an element
			if (includeEqualSuperClasses && ancestors.size() == previousAncestorSize) {
				ontology.equivalentClassesAxioms(ancestors.get(i)).forEach(axiom -> {
					axiom.classExpressions().forEach(elem -> {
						// System.out.println(elem.conjunctSet().findFirst());
						if (!ancestors.contains(elem.conjunctSet().findFirst().get().asOWLClass())) {
							ancestors.add(elem.conjunctSet().findFirst().get().asOWLClass());
						}
					});
				});
			}
			// If we have found the last element, stop
			if (ancestors.get(ancestors.size() - 1).getIRI().getShortForm().equals(until)) {
				break;
			}
		}

		// Get names and invert order
		List<String> ancestorNames = new ArrayList<String>();
		boolean startAdding = true;
		for (int i = ancestors.size() - 1; i >= 0; i--) {
			if (!startAdding) {
				String ancestorName = ancestors.get(i).getIRI().getShortForm();
				if (ancestorName.equals(until)) {
					ancestorNames.add(ancestorName);
					startAdding = true;
				}
			} else {
				ancestorNames.add(ancestors.get(i).getIRI().getShortForm());
			}
		}
		ancestorNames.add(algorithmAsIndividual.getIRI().getShortForm());

		return ancestorNames;
	}

	private String getAsOntologyElement(String name) {
		StringBuilder builder = new StringBuilder();
		builder.append(ontologyIRI);
		builder.append(ontologyIRISeparator);
		builder.append(name);

		return builder.toString();
	}

	/**
	 * Get the ontology this connector uses.
	 * 
	 * @return The used ontology
	 */
	public OWLOntology getOntology() {
		return ontology;
	}

	/**
	 * Get the fully qualified names of WEKA classifiers that this ontology
	 * connector can be queried for.
	 * 
	 * @return The available classifiers
	 */
	public List<String> getAvailableClassifiers() {
		return classifierPortfolio;
	}

	/**
	 * Get the fully qualified names of WEKA ASSearch algorithms that this ontology
	 * can be queried for.
	 * 
	 * @return THe available searchers
	 */
	public List<String> getAvailableSearchers() {
		return searcherPortfolio;
	}

	/**
	 * Get the fully qualified names of WEKA ASEvaluation algorithms that this
	 * ontology can be queried for.
	 * 
	 * @return The evailable evaluators
	 */
	public List<String> getAvailableEvaluators() {
		return evaluatorPortfolio;
	}

	public static void main(String[] args) throws OWLOntologyCreationException {
		WEKAOntologyConnector connector = new WEKAOntologyConnector();

		for (String classifier : classifierPortfolio) {
			System.out.println(connector.getAncestorsOfClassifier(classifier));
		}

		for (String searcher : searcherPortfolio) {
			System.out.println(connector.getAncestorsOfSearcher(searcher));
		}

		for (String evaluator : evaluatorPortfolio) {
			System.out.println(connector.getAncestorsOfEvaluator(evaluator));
		}

	}

	/**
	 * Get the highest common node in the ontology for all classifiers
	 * 
	 * @return The classifier top node
	 */
	public String getClassifierTopNode() {
		return classifierTopNode;
	}

	/**
	 * Get the highest common node in the ontology for all searchers
	 * 
	 * @return The searcher top node
	 */
	public String getSearcherTopNode() {
		return searcherTopNode;
	}

	/**
	 * Get the highest common node in the ontology for all evaluators
	 * 
	 * @return The evaluator top node
	 */
	public String getEvaluatorTopNode() {
		return evaluatorTopNode;
	}
}
