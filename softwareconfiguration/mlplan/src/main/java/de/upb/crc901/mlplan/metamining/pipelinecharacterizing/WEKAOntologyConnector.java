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

	/**
	 * Location of the ontology used by this connector
	 */
	private static final String ONTOLOGY_FILE_NAME = "DMOP_modified.owl";

	/**
	 * IRI of the elements in this ontology
	 */
	private static final String ONTOLOGY_IRI = "http://www.e-lico.eu/ontologies/dmo/DMOP/DMOP.owl";

	/**
	 * Separator that separates the ontology IRI from the name of an ontology
	 * element
	 */
	private static final String ONTOLOGY_IRI_SEPARATOR = "#";

	/**
	 * List of all classifiers that can be characterized by this ontology connector
	 */
	private static final List<String> classifierPortfolio = Arrays.asList("weka.classifiers.bayes.BayesNet",
			"weka.classifiers.bayes.NaiveBayes", "weka.classifiers.bayes.NaiveBayesMultinomial",
			"weka.classifiers.functions.Logistic", "weka.classifiers.functions.MultilayerPerceptron",
			"weka.classifiers.functions.SGD", "weka.classifiers.functions.SimpleLogistic",
			"weka.classifiers.functions.SMO", "weka.classifiers.functions.VotedPerceptron", "weka.classifiers.lazy.IBk",
			"weka.classifiers.lazy.KStar", "weka.classifiers.rules.DecisionTable", "weka.classifiers.rules.JRip",
			"weka.classifiers.rules.OneR", "weka.classifiers.rules.PART", "weka.classifiers.rules.ZeroR",
			"weka.classifiers.trees.DecisionStump", "weka.classifiers.trees.J48", "weka.classifiers.trees.LMT",
			"weka.classifiers.trees.RandomForest", "weka.classifiers.trees.RandomTree",
			"weka.classifiers.trees.REPTree", "weka.classifiers.meta.Vote", "weka.classifiers.meta.Stacking",
			"weka.classifiers.meta.RandomSubSpace", "weka.classifiers.meta.RandomCommittee",
			"weka.classifiers.meta.MultiClassClassifier", "weka.classifiers.meta.LogitBoost",
			"weka.classifiers.meta.ClassificationViaRegression", "weka.classifiers.meta.Bagging",
			"weka.classifiers.meta.AdditiveRegression", "weka.classifiers.meta.AdaBoostM1",
			"weka.classifiers.trees.M5P", "weka.classifiers.rules.M5Rules",
			"weka.classifiers.functions.SimpleLinearRegression");

	/**
	 * List of all evaluators (for a data-preprocessor) that can be characterized by
	 * this ontology connector
	 */
	private static final List<String> evaluatorPortfolio = Arrays.asList("weka.attributeSelection.CfsSubsetEval",
			"weka.attributeSelection.CorrelationAttributeEval", "weka.attributeSelection.GainRatioAttributeEval",
			"weka.attributeSelection.InfoGainAttributeEval", "weka.attributeSelection.OneRAttributeEval",
			"weka.attributeSelection.PrincipalComponents", "weka.attributeSelection.ReliefFAttributeEval",
			"weka.attributeSelection.SymmetricalUncertAttributeEval");

	/**
	 * List of all searchers (for a data-preprocessor) that can be characterized by
	 * this ontology connector
	 */
	private static final List<String> searcherPortfolio = Arrays.asList("weka.attributeSelection.BestFirst",
			"weka.attributeSelection.GreedyStepwise", "weka.attributeSelection.Ranker");

	/**
	 * List of all kernel functions that can be characterized by this ontology
	 * connector
	 */
	private static final List<String> kernelFunctionPortfolio = Arrays.asList(
			"weka.classifiers.functions.supportVector.Puk", "weka.classifiers.functions.supportVector.RBFKernel",
			"weka.classifiers.functions.supportVector.PolyKernel",
			"weka.classifiers.functions.supportVector.NormalizedPolyKernel");

	/**
	 * The common ancestor of all classifiers in the ontology
	 */
	private static final String CLASSIFIER_TOP_NODE = "ModelingAlgorithm";

	/**
	 * The common ancestor of all searchers in the ontology
	 */
	private static final String SEARCHER_TOP_NODE = "DataProcessingAlgorithm";

	/**
	 * The common ancestor of all evaluator in the ontology
	 */
	private static final String EVALUTATOR_TOP_NODE = "DataProcessingAlgorithm";

	/**
	 * The common ancestor of all kernel functions in the ontology
	 */
	private static final String KERNEL_FUNCTION_TOP_NODE = "KernelFunction";

	/**
	 * The data factory used to get ontology elements from Strings
	 */
	private OWLDataFactory dataFactory;

	/**
	 * The used ontology as an object
	 */
	private OWLOntology ontology;

	/**
	 * Whether equals relations shall also be included in the returned
	 * characterization
	 */
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
		InputStream inputStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(ONTOLOGY_FILE_NAME);
		ontology = ontologyManager.loadOntologyFromOntologyDocument(inputStream);
	}

	@Override
	public List<String> getAncestorsOfAlgorithm(String algorithmName) {
		if (classifierPortfolio.contains(algorithmName)) {
			return getAncestorsOfAlgorithmUntil(algorithmName, CLASSIFIER_TOP_NODE);
		} else if (searcherPortfolio.contains(algorithmName)) {
			return getAncestorsOfAlgorithmUntil(algorithmName, SEARCHER_TOP_NODE);
		} else if (evaluatorPortfolio.contains(algorithmName)) {
			return getAncestorsOfAlgorithmUntil(algorithmName, EVALUTATOR_TOP_NODE);
		} else if (kernelFunctionPortfolio.contains(algorithmName)) {
			return getAncestorsOfAlgorithmUntil(algorithmName, KERNEL_FUNCTION_TOP_NODE);
		} else {
			StringBuilder builder = new StringBuilder();
			builder.append(algorithmName);
			builder.append(" is not supported by the used ontology.");
			throw new IllegalArgumentException(builder.toString());
		}
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
		
		// Get ancestors
		ArrayList<OWLClass> ancestors = new ArrayList<>();
		ontology.classAssertionAxioms(algorithmAsIndividual).findFirst().ifPresent(algorithmClass -> ancestors.add(algorithmClass.getClassExpression().asOWLClass()));
		for (int i = 0; i < ancestors.size(); i++) {

			// If we have found the last element, stop
			if (ancestors.get(ancestors.size() - 1).getIRI().getShortForm().equals(until)) {
				break;
			}

			int previousAncestorSize = ancestors.size();
			ontology.subClassAxiomsForSubClass(ancestors.get(i))
					.filter(axiom -> axiom.getSuperClass().getClassExpressionType() == ClassExpressionType.OWL_CLASS)
					.forEach(axiom -> {
						OWLClass toAdd = axiom.getSuperClass().asOWLClass();
						ancestors.add(toAdd);
					});
			// If we have not added an element
			if (includeEqualSuperClasses && ancestors.size() == previousAncestorSize) {
				ontology.equivalentClassesAxioms(ancestors.get(i)).forEach(axiom -> 
					axiom.classExpressions().forEach(elem -> {
						if (!ancestors.contains(elem.conjunctSet().findFirst().get().asOWLClass())) {
							ancestors.add(elem.conjunctSet().findFirst().get().asOWLClass());
						}
					})
				);
			}

		}

		// Get names and invert order
		List<String> ancestorNames = new ArrayList<>();

		for (int i = ancestors.size() - 1; i >= 0; i--) {
			String ancestorName = ancestors.get(i).getIRI().getShortForm();
			if (ancestorName.equals(until)) {
				ancestorNames.add(ancestorName);
			}
		}
		ancestorNames.add(algorithmAsIndividual.getIRI().getShortForm());

		return ancestorNames;
	}

	/**
	 * Appends the given name of an ontology element to the IRI of the used
	 * ontology, separated by a specified separator.
	 * 
	 * @param name
	 *            The name of the ontology element
	 * @return The fully qualified name of the ontology element
	 */
	private String getAsOntologyElement(String name) {
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
	 * @return The available searchers
	 */
	public List<String> getAvailableSearchers() {
		return searcherPortfolio;
	}

	/**
	 * Get the fully qualified names of WEKA ASEvaluation algorithms that this
	 * ontology can be queried for.
	 * 
	 * @return The available evaluators
	 */
	public List<String> getAvailableEvaluators() {
		return evaluatorPortfolio;
	}

	/**
	 * Get the fully qualified names of kernel functions that this ontology can be
	 * queried for
	 * 
	 * @return
	 */
	public List<String> getAvailableKernelFunctions() {
		return kernelFunctionPortfolio;
	}

	/**
	 * Get the highest common node in the ontology for all classifiers
	 * 
	 * @return The classifier top node
	 */
	public String getClassifierTopNode() {
		return CLASSIFIER_TOP_NODE;
	}

	/**
	 * Get the highest common node in the ontology for all searchers
	 * 
	 * @return The searcher top node
	 */
	public String getSearcherTopNode() {
		return SEARCHER_TOP_NODE;
	}

	/**
	 * Get the highest common node in the ontology for all evaluators
	 * 
	 * @return The evaluator top node
	 */
	public String getEvaluatorTopNode() {
		return EVALUTATOR_TOP_NODE;
	}

	/**
	 * Get the highest common node in the ontology for all kernel function
	 * 
	 * @return The kernel function top node
	 */
	public String getKernelFunctionTopNode() {
		return KERNEL_FUNCTION_TOP_NODE;
	}
}
