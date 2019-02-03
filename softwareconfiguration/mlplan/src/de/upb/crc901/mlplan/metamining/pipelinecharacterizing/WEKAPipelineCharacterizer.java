package de.upb.crc901.mlplan.metamining.pipelinecharacterizing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model.MLPipeline;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model.SupervisedFilterSelector;
import treeminer.FrequentSubtreeFinder;
import treeminer.TreeMiner;
import treeminer.TreeRepresentationUtils;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;

/**
 * A characterizer for MLPipelines that characterizes them using an ontology and
 * a tree mining algorithm.
 * 
 * @author Helena Graf
 *
 */
public class WEKAPipelineCharacterizer implements IPipelineCharacterizer {

	private FrequentSubtreeFinder treeMiner;
	private IOntologyConnector ontologyConnector;
	private String[] patterns;
	private int minSupport = 1;
	private String preprocessorSubTreeName = "Preprocessor";
	private String preprocessorsSubTreeName = "Preprocessors";
	private String pipelineTreeName = "Pipeline";

	public WEKAPipelineCharacterizer() {
		treeMiner = new TreeMiner();
		try {
			ontologyConnector = new WEKAOntologyConnector();
		} catch (OWLOntologyCreationException e) {
			System.err.println("Cannot connect to Ontology!");
			throw new RuntimeException(e);
		}
	}

	@Override
	public void build(List<MLPipeline> pipelines) {
		// Convert the pipelines to String representations
		List<String> pipelineRepresentations = new ArrayList<String>();
		pipelines.forEach(pipeline -> {
			pipelineRepresentations.add(makeStringTreeRepresentation(pipeline));
		});

		// Use the tree miner to find patterns
		treeMiner.findFrequentSubtrees(pipelineRepresentations, minSupport);
	}

	@Override
	public double[] characterize(MLPipeline pipeline) {
		// Make tree representation from this pipeline
		String treeRepresentation = makeStringTreeRepresentation(pipeline);

		// Ask the treeMiner which of the patterns are included in this pipeline
		double[] pipelineCharacterization = new double[patterns.length];
		for (int i = 0; i < patterns.length; i++) {
			if (TreeRepresentationUtils.containsSubtree(treeRepresentation, patterns[i])) {
				pipelineCharacterization[i] = 1;
			} else {
				pipelineCharacterization[i] = 0;
			}
		}

		return pipelineCharacterization;
	}

	/**
	 * Converts the given MLPipeline to a String representation of its components
	 * using the ontology
	 * 
	 * @param pipeline
	 * @return
	 */
	protected String makeStringTreeRepresentation(MLPipeline pipeline) {
		// TODO add hyperparameters of the algorithms

		// Get annotations for preprocessors
		List<String> preprocessorsSubTree = new ArrayList<String>();
		List<SupervisedFilterSelector> preprocessors = pipeline.getPreprocessors();
		preprocessors.forEach(preprocessor -> {
			// Get searcher annotation
			String searcher = preprocessor.getSearcher().getClass().getName();
			List<String> searcherBranch = ontologyConnector.getAncestorsOfSearcher(searcher);
			String searcherBranchRepresentation = TreeRepresentationUtils.makeRepresentationForBranch(searcherBranch);

			// Get evaluator annotation
			String evaluator = preprocessor.getEvaluator().getClass().getName();
			List<String> evaluatorBranch = ontologyConnector.getAncestorsOfEvaluator(evaluator);
			String evaluatorBranchRepresentation = TreeRepresentationUtils.makeRepresentationForBranch(evaluatorBranch);

			// Merge both annotations
			String preprocessorSubTree = TreeRepresentationUtils.addChildrenToNode(preprocessorSubTreeName,
					Arrays.asList(searcherBranchRepresentation, evaluatorBranchRepresentation));
			preprocessorsSubTree.add(preprocessorSubTree);
		});
		// Merge preprocessors
		String preprocessorsSubTreeRepresentation = TreeRepresentationUtils.addChildrenToNode(preprocessorsSubTreeName,
				preprocessorsSubTree);

		// Get annotations for classifier
		String classifier = pipeline.getBaseClassifier().getClass().getName();
		List<String> classifierBranch = ontologyConnector.getAncestorsOfClassifier(classifier);
		String classifierBranchRepresentation = TreeRepresentationUtils.makeRepresentationForBranch(classifierBranch);

		// Merge preprocessors and classifiers
		return TreeRepresentationUtils.addChildrenToNode(pipelineTreeName,
				Arrays.asList(preprocessorsSubTreeRepresentation, classifierBranchRepresentation));
	}
	
	private List<String> getParametersForClassifier(Classifier classifier) {
		List<String> parameters = new ArrayList<String>();
		
		// Check if classifier has options
		if (classifier instanceof AbstractClassifier) {
			AbstractClassifier abstractClassifier = (AbstractClassifier) classifier;
			if (abstractClassifier.getOptions() != null && abstractClassifier.getOptions().length > 0) {
				
				// Get options
				abstractClassifier.getOptions();
				//TODO
			}
		} 
		
		return parameters;
	}

	@Override
	public double[][] getCharacterizationsOfTrainingExamples() {
		// TODO Implement
		// TODO maybe adjust return parameter type here
		return null;
	}

	/**
	 * @return the ontologyConnector
	 */
	public IOntologyConnector getOntologyConnector() {
		return ontologyConnector;
	}

	/**
	 * @param ontologyConnector
	 *            the ontologyConnector to set
	 */
	public void setOntologyConnector(IOntologyConnector ontologyConnector) {
		this.ontologyConnector = ontologyConnector;
	}

	/**
	 * Get the minimum support required for a pattern to be considered frequent for
	 * the tree mining algorithm.
	 * 
	 * @return The minimum support a tree pattern must have to be considered
	 *         frequent
	 */
	public int getMinSupport() {
		return minSupport;
	}

	/**
	 * Set the minimum support required for a pattern to be considered frequent for
	 * the tree mining algorithm.
	 * 
	 * @param minSupport
	 *            The minimum support a tree pattern must have to be considered
	 *            frequent
	 */
	public void setMinSupport(int minSupport) {
		this.minSupport = minSupport;
	}

}
