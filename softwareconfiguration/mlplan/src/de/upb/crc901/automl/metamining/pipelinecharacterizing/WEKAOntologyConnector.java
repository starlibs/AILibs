package de.upb.crc901.automl.metamining.pipelinecharacterizing;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class WEKAOntologyConnector implements IOntologyConnector {

	private String ontologyFileName = "DMOP.owl";
	// TODO can get this from ontology object
	private String ontologyURI = "http://www.e-lico.eu/ontologies/dmo/DMOP/DMOP.owl";
	private String ontologyURISeparator = "#";
	private OWLDataFactory dataFactory;
	private OWLOntology ontology;

	// TODO fill this in
	private String classifierTopNode = "";
	private String searcherTopNode = "";
	private String evaluatorTopNode = "";

	/**
	 * Creates an ontology connector using the specified ontology.
	 * 
	 * @param ontology
	 *            The ontology this connector will reference
	 * @throws OWLOntologyCreationException
	 *             If the ontology cannot be created
	 */
	public WEKAOntologyConnector(String ontology) throws OWLOntologyCreationException {
		// TODO
	}

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
		return getAncestorsOfAlgorithmUntil(classifierName, classifierTopNode);
	}

	@Override
	public List<String> getAncestorsOfSearcher(String searcher) {
		return getAncestorsOfAlgorithmUntil(searcher, searcherTopNode);
	}

	@Override
	public List<String> getAncestorsOfEvaluator(String evaluator) {
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
		// Get the algorithm
		StringBuilder builder = new StringBuilder();
		builder.append(ontologyURI);
		builder.append(ontologyURISeparator);
		builder.append(algorithm);
		OWLClass child = dataFactory.getOWLClass(builder.toString());

		// Get ancestors
		ArrayList<OWLClass> ancestors = new ArrayList<OWLClass>();
		ancestors.add(child);
		for (int i = 0; i < ancestors.size(); i++) {
			ontology.subClassAxiomsForSubClass(ancestors.get(i))
					.filter(axiom -> axiom.getSuperClass().getClassExpressionType() == ClassExpressionType.OWL_CLASS
							&& axiom.getSuperClass().asOWLClass().getIRI().getShortForm().equals(until))
					.forEach(axiom -> {
						OWLClass toAdd = axiom.getSuperClass().asOWLClass();
						ancestors.add(toAdd);
					});
		}

		// Get names and invert order
		List<String> ancestorNames = new ArrayList<String>();
		for (int i = ancestors.size() - 1; i >= 0; i++) {
			ancestorNames.add(ancestors.get(i).getIRI().getShortForm());
		}

		return ancestorNames;
	}

	/**
	 * Get ontology this connector uses.
	 * 
	 * @return THe used ontology
	 */
	public OWLOntology getOntology() {
		return ontology;
	}
}
