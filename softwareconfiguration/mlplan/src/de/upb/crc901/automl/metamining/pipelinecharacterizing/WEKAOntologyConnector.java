package de.upb.crc901.automl.metamining.pipelinecharacterizing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class WEKAOntologyConnector implements IOntologyConnector {
	
	private HashMap<String, String> mapAlgorithmsToOperators;

	private static String dmopObsolete = "DMOPobsolete.owl";
	private static String defaultOntology = "DMOP.owl";

	public void loadOntology() throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = ontologyManager.getOWLDataFactory();
		InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(defaultOntology);
		OWLOntology ontology = ontologyManager.loadOntologyFromOntologyDocument(inputStream);
		// OWLOntology ontology =
		// ontologyManager.loadOntology(IRI.create("http://www.e-lico.eu/ontologies/dmo/DMOP/DMOP.owl#"));
		// OutputStream outputStream = new
		// FileOutputStream("resources/DMOPobsolete.owl");
		// ontologyManager.saveOntology(ontology, outputStream);

		// inputStream =
		// Thread.currentThread().getContextClassLoader().getResourceAsStream(defaultOntology);
		// OWLOntology dmop =
		// ontologyManager.loadOntologyFromOntologyDocument(inputStream);
		System.out.println(ontology);

		// Reasoner hermit = new Reasoner(null, ontology);
		// OWLReasonerFactory reasonerFactory = new ReasonerFactory();
		// OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
		// reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

		OWLClass naiveBayes = dataFactory
				.getOWLClass("http://www.e-lico.eu/ontologies/dmo/DMOP/DMOP.owl#NaiveBayesAlgorithm");
		// reasoner.getSuperClasses(naiveBayes).forEach(node -> {
		// System.out.println(node);
		// });
		//
		// reasoner.load
//		OWLClass hasQuality = dataFactory.getOWLClass("http://www.e-lico.eu/ontologies/dmo/DMOP/DMOP.owl#has-quality");
//
//		ontology.subClassAxiomsForSubClass(naiveBayes).forEach(classs -> {
//			System.out.println(classs.getSuperClass().getClassExpressionType() == ClassExpressionType.OWL_CLASS);
//			System.out.println(classs);
//			if (classs.getSuperClass().getClassExpressionType() == ClassExpressionType.OWL_CLASS) {
//
//			}
//		});

		// have found, class, now iteratively get all equals & ancestors
		ArrayList<OWLClass> ancestors = new ArrayList<OWLClass>();
		ancestors.add(naiveBayes);
		for (int i = 0; i < ancestors.size(); i++) {
			ontology.subClassAxiomsForSubClass(ancestors.get(i)).forEach(axiom -> System.out.println("Found: " + axiom));
			
			// get ancestors
			ontology.subClassAxiomsForSubClass(ancestors.get(i))
					.filter(axiom -> axiom.getSuperClass().getClassExpressionType() == ClassExpressionType.OWL_CLASS)
					.forEach(axiom -> {
						OWLClass toAdd = axiom.getSuperClass().asOWLClass();
						System.out.println("Adding: " + toAdd);
						ancestors.add(toAdd);
					});
		}
	}
	
	

	public static void main(String[] args)
			throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
		new WEKAOntologyConnector().loadOntology();

	}



	@Override
	public List<String> getParentsOfClassifier(String classifierName) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public List<String> getParentsOfSearcher(String searcher) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public List<String> getParentsOfEvaluator(String evaluator) {
		// TODO Auto-generated method stub
		return null;
	}
}
