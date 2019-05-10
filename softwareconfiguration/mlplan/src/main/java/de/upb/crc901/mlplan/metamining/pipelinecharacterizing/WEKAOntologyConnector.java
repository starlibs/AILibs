package de.upb.crc901.mlplan.metamining.pipelinecharacterizing;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final List<String> classifierPortfolio = Arrays.asList("weka.classifiers.bayes.BayesNet", "weka.classifiers.bayes.NaiveBayes", "weka.classifiers.bayes.NaiveBayesMultinomial", "weka.classifiers.functions.Logistic",
			"weka.classifiers.functions.MultilayerPerceptron", "weka.classifiers.functions.SGD", "weka.classifiers.functions.SimpleLogistic", "weka.classifiers.functions.SMO", "weka.classifiers.functions.VotedPerceptron",
			"weka.classifiers.lazy.IBk", "weka.classifiers.lazy.KStar", "weka.classifiers.rules.DecisionTable", "weka.classifiers.rules.JRip", "weka.classifiers.rules.OneR", "weka.classifiers.rules.PART", "weka.classifiers.rules.ZeroR",
			"weka.classifiers.trees.DecisionStump", "weka.classifiers.trees.J48", "weka.classifiers.trees.LMT", "weka.classifiers.trees.RandomForest", "weka.classifiers.trees.RandomTree", "weka.classifiers.trees.REPTree");

	private static final List<String> evaluatorPortfolio = Arrays.asList("weka.attributeSelection.CfsSubsetEval", "weka.attributeSelection.CorrelationAttributeEval", "weka.attributeSelection.GainRatioAttributeEval",
			"weka.attributeSelection.InfoGainAttributeEval", "weka.attributeSelection.OneRAttributeEval", "weka.attributeSelection.PrincipalComponents", "weka.attributeSelection.ReliefFAttributeEval",
			"weka.attributeSelection.SymmetricalUncertAttributeEval");

	private static final List<String> searcherPortfolio = Arrays.asList("weka.attributeSelection.BestFirst", "weka.attributeSelection.GreedyStepwise", "weka.attributeSelection.Ranker");

	private static final String ONTOLOGY_FILENAME = "DMOP_modified.owl";
	private static final String ONTOLOGY_IRI = "http://www.e-lico.eu/ontologies/dmo/DMOP/DMOP.owl";
	private static final String ONTOLOGY_IRI_SEPARATOR = "#";

	private static final Logger logger = LoggerFactory.getLogger(WEKAOntologyConnector.class);

	private String classifierTopNode = "ModelingAlgorithm";
	private String searcherTopNode = "SearchStrategy";
	private String evaluatorTopNode = "DataProcessingAlgorithm";

	private OWLDataFactory dataFactory;
	private OWLOntology ontology;
	private boolean includeEqualSuperClasses = true;

	private static final String MSG_NOT_SUPPORTED_BY_ONTOLOGY = " is not supported by the used ontology.";

	/**
	 * Creates an ontology connector using the standard ontology.
	 *
	 * @throws OWLOntologyCreationException
	 *             If the ontology cannot be created
	 */
	public WEKAOntologyConnector() throws OWLOntologyCreationException {
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		this.dataFactory = ontologyManager.getOWLDataFactory();
		InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(ONTOLOGY_FILENAME);
		this.ontology = ontologyManager.loadOntologyFromOntologyDocument(inputStream);
	}

	@Override
	public List<String> getAncestorsOfClassifier(final String classifierName) {
		if (!classifierPortfolio.contains(classifierName)) {
			StringBuilder builder = new StringBuilder();
			builder.append(classifierName);
			builder.append(MSG_NOT_SUPPORTED_BY_ONTOLOGY);
			throw new IllegalArgumentException(builder.toString());
		}
		return this.getAncestorsOfAlgorithmUntil(classifierName, this.classifierTopNode);
	}

	@Override
	public List<String> getAncestorsOfSearcher(final String searcher) {
		if (!searcherPortfolio.contains(searcher)) {
			StringBuilder builder = new StringBuilder();
			builder.append(searcher);
			builder.append(MSG_NOT_SUPPORTED_BY_ONTOLOGY);
			throw new IllegalArgumentException(builder.toString());
		}
		return this.getAncestorsOfAlgorithmUntil(searcher, this.searcherTopNode);
	}

	@Override
	public List<String> getAncestorsOfEvaluator(final String evaluator) {
		if (!evaluatorPortfolio.contains(evaluator)) {
			StringBuilder builder = new StringBuilder();
			builder.append(evaluator);
			builder.append(MSG_NOT_SUPPORTED_BY_ONTOLOGY);
			throw new IllegalArgumentException(builder.toString());
		}
		return this.getAncestorsOfAlgorithmUntil(evaluator, this.evaluatorTopNode);
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
	protected List<String> getAncestorsOfAlgorithmUntil(final String algorithm, final String until) {
		// Get the individual represented by the algorithm
		OWLNamedIndividual algorithmAsIndividual = this.dataFactory.getOWLNamedIndividual(this.getAsOntologyElement(algorithm));

		Optional<OWLClassAssertionAxiom> firstAxiom = this.ontology.classAssertionAxioms(algorithmAsIndividual).findFirst();
		if (!firstAxiom.isPresent()) {
			throw new IllegalArgumentException("There is no axiom for the given algorithm");
		}

		OWLClass algorithmClass = firstAxiom.get().getClassExpression().asOWLClass();
		// Get ancestors
		ArrayList<OWLClass> ancestors = new ArrayList<>();
		ancestors.add(algorithmClass);
		for (int i = 0; i < ancestors.size(); i++) {
			int previousAncestorSize = ancestors.size();
			this.ontology.subClassAxiomsForSubClass(ancestors.get(i)).filter(axiom -> axiom.getSuperClass().getClassExpressionType() == ClassExpressionType.OWL_CLASS).forEach(axiom -> {
				OWLClass toAdd = axiom.getSuperClass().asOWLClass();
				ancestors.add(toAdd);
			});
			// If we have not added an element
			if (this.includeEqualSuperClasses && ancestors.size() == previousAncestorSize) {
				this.ontology.equivalentClassesAxioms(ancestors.get(i)).forEach(axiom -> axiom.classExpressions().forEach(elem -> {
					if (!ancestors.contains(elem.conjunctSet().findFirst().get().asOWLClass())) {
						ancestors.add(elem.conjunctSet().findFirst().get().asOWLClass());
					}
				}));
			}
			// If we have found the last element, stop
			if (ancestors.get(ancestors.size() - 1).getIRI().getShortForm().equals(until)) {
				break;
			}
		}

		// Get names and invert order
		List<String> ancestorNames = new ArrayList<>();
		boolean startAdding = true;
		for (int i = ancestors.size() - 1; i >= 0; i--) {
			if (!startAdding) {
				String ancestorName = ancestors.get(i).getIRI().getShortForm();
				if (ancestorName.equals(until)) {
					ancestorNames.add(ancestorName);
				}
			} else {
				ancestorNames.add(ancestors.get(i).getIRI().getShortForm());
				startAdding = false;
			}
		}
		ancestorNames.add(algorithmAsIndividual.getIRI().getShortForm());

		return ancestorNames;
	}

	private String getAsOntologyElement(final String name) {
		StringBuilder builder = new StringBuilder();
		builder.append(ONTOLOGY_IRI);
		builder.append(ONTOLOGY_IRI_SEPARATOR);
		builder.append(name);

		return builder.toString();
	}

	/**
	 * Get the ontology this connector uses.
	 *
	 * @return The used ontology
	 */
	public OWLOntology getOntology() {
		return this.ontology;
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

	public static void main(final String[] args) throws OWLOntologyCreationException {
		WEKAOntologyConnector connector = new WEKAOntologyConnector();

		for (String classifier : classifierPortfolio) {
			logger.info("{}", connector.getAncestorsOfClassifier(classifier));
		}

		for (String searcher : searcherPortfolio) {
			logger.info("{}", connector.getAncestorsOfSearcher(searcher));
		}

		for (String evaluator : evaluatorPortfolio) {
			logger.info("{}", connector.getAncestorsOfEvaluator(evaluator));
		}

	}

	/**
	 * Get the highest common node in the ontology for all classifiers
	 *
	 * @return The classifier top node
	 */
	public String getClassifierTopNode() {
		return this.classifierTopNode;
	}

	/**
	 * Get the highest common node in the ontology for all searchers
	 *
	 * @return The searcher top node
	 */
	public String getSearcherTopNode() {
		return this.searcherTopNode;
	}

	/**
	 * Get the highest common node in the ontology for all evaluators
	 *
	 * @return The evaluator top node
	 */
	public String getEvaluatorTopNode() {
		return this.evaluatorTopNode;
	}
}
