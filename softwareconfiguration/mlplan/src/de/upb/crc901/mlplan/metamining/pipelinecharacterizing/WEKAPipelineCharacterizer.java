package de.upb.crc901.mlplan.metamining.pipelinecharacterizing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;
import treeminer.FrequentSubtreeFinder;
import treeminer.TreeMiner;
import treeminer.TreeRepresentationUtils;

/**
 * A characterizer for MLPipelines. It characterizes pipelines using an ontology
 * and a tree mining algorithm. The ontology is used to get a characterization
 * of a pipeline element; from the characterization of all pipelines elements
 * and their parameters, a tree is then built. The trees retrieved from a number
 * of training examples for pipelines are then used to find frequent patterns in
 * the pipelines. A new pipeline is then characterizes by which of these
 * patterns appear in it.
 * 
 * @author Helena Graf
 *
 */
public class WEKAPipelineCharacterizer implements IPipelineCharacterizer {

	/**
	 * Number of concurrent threads maximally used by the characterizer
	 */
	private int CPUs = 1;

	/**
	 * The ontology connector used to characterize a single pipeline element
	 */
	private IOntologyConnector ontologyConnector;

	/**
	 * The algorithm used by the pipeline characterizer to find frequent subtrees in
	 * deduced tree representations of given pipelines
	 */
	private FrequentSubtreeFinder treeMiner;

	/**
	 * The frequent patterns found in the tree representations of pipelines by the
	 * tree mining algorithm
	 */
	private List<String> foundPipelinePatterns;

	/**
	 * The minimum support required for a pattern to be considered frequent by the
	 * tree miner
	 */
	private int patternMinSupport = 5;

	private Map<Component, Map<Parameter, ParameterRefinementConfiguration>> componentParameters;

	/**
	 * Creates a new pipeline characterizer that uses the given descriptions of
	 * parameters to characterize MLPipelines.
	 * 
	 * @param componentParameters
	 *            The description of parameters in the current configuration
	 *            together with their refinements.
	 */
	public WEKAPipelineCharacterizer(
			Map<Component, Map<Parameter, ParameterRefinementConfiguration>> componentParameters) {
		this.treeMiner = new TreeMiner();
		this.componentParameters = componentParameters;

		try {
			ontologyConnector = new WEKAOntologyConnector();
		} catch (OWLOntologyCreationException e) {
			System.err.println("Cannot connect to Ontology!");
			throw new RuntimeException(e);
		}
	}

	@Override
	public void build(List<ComponentInstance> pipelines) throws InterruptedException {
		// Convert the pipelines to String representations
		System.out.println("WEKAPipelineCharacterizer: Converting training examples to trees.");

		int chunkSize = Math.floorDiv(pipelines.size(), CPUs);
		int lastchunkSize = pipelines.size() - (chunkSize * (CPUs - 1));

		ComponentInstanceStringConverter[] threads = new ComponentInstanceStringConverter[CPUs];

		for (int i = 0; i < threads.length; i++) {
			threads[i] = new ComponentInstanceStringConverter(ontologyConnector,
					pipelines.subList(i * chunkSize,
							i == threads.length - 1 ? (i * chunkSize) + lastchunkSize : (i + 1) * chunkSize),
					componentParameters);
			threads[i].start();
		}

		List<String> pipelineRepresentations = new ArrayList<String>(pipelines.size());
		for (int i = 0; i < threads.length; i++) {
			threads[i].join();
			pipelineRepresentations.addAll(threads[i].getConvertedPipelines());
		}

		// Use the tree miner to find patterns
		System.out.println("WEKAPipelineCharacterizer: Find frequent subtrees.");
		foundPipelinePatterns = treeMiner.findFrequentSubtrees(pipelineRepresentations, patternMinSupport);
	}

	@Override
	public double[] characterize(ComponentInstance pipeline) {
		// Make tree representation from this pipeline
		String treeRepresentation = new ComponentInstanceStringConverter(ontologyConnector, new ArrayList<>(),
				componentParameters).makeStringTreeRepresentation(pipeline);

		// Ask the treeMiner which of the patterns are included in this pipeline
		double[] pipelineCharacterization = new double[foundPipelinePatterns.size()];
		for (int i = 0; i < foundPipelinePatterns.size(); i++) {
			if (TreeRepresentationUtils.containsSubtree(treeRepresentation, foundPipelinePatterns.get(i))) {
				pipelineCharacterization[i] = 1;
			} else {
				pipelineCharacterization[i] = 0;
			}
		}
		return pipelineCharacterization;
	}

	@Override
	public double[][] getCharacterizationsOfTrainingExamples() {
		return treeMiner.getCharacterizationsOfTrainingExamples();
	}

	/**
	 * Get the used ontology connector.
	 * 
	 * @return The used ontology connector
	 */
	public IOntologyConnector getOntologyConnector() {
		return ontologyConnector;
	}

	/**
	 * Set the ontology connector to be used.
	 * 
	 * @param ontologyConnector
	 *            the ontologyConnector to be used
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
		return patternMinSupport;
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
		this.patternMinSupport = minSupport;
	}

	/**
	 * Inform the Characterizer about resource usage.
	 * 
	 * @param cPUs
	 *            Maximum number of threads that will be used by the characterizer
	 */
	public void setCPUs(int cPUs) {
		CPUs = cPUs;
	}

	/**
	 * Get the patterns found among the given training examples.
	 * 
	 * @return A list of patterns
	 */
	public List<String> getFoundPipelinePatterns() {
		return foundPipelinePatterns;
	}

}
