package jaicore.ml.metafeatures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.time.StopWatch;
import org.openml.webapplication.fantail.dc.Characterizer;
import org.openml.webapplication.fantail.dc.landmarking.GenericLandmarker;
import org.openml.webapplication.fantail.dc.statistical.Cardinality;
import org.openml.webapplication.fantail.dc.statistical.NominalAttDistinctValues;
import org.openml.webapplication.fantail.dc.statistical.SimpleMetaFeatures;
import org.openml.webapplication.fantail.dc.statistical.Statistical;
import org.openml.webapplication.features.GlobalMetafeatures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Instances;
import weka.core.Utils;

/**
 * Characterizer that applies a number of Characterizers to a data set. Uses
 * probing. Adapted from {@link GlobalMetafeatures}.
 * 
 * @author Helena Graf
 *
 */
public class GlobalCharacterizer extends Characterizer {

	private final Logger logger = LoggerFactory.getLogger(GlobalCharacterizer.class);

	// preprocessor prefixes
	protected static final String PREPROCESSING_PREFIX = "-E \"weka.attributeSelection.CfsSubsetEval -P 1 -E 1\" -S \"weka.attributeSelection.BestFirst -D 1 -N 5\" -W ";
	protected static final String CP_IBK = "weka.classifiers.lazy.IBk";
	protected static final String CP_NB = "weka.classifiers.bayes.NaiveBayes";
	protected static final String CP_ASC = "weka.classifiers.meta.AttributeSelectedClassifier";
	protected static final String CP_DS = "weka.classifiers.trees.DecisionStump";

	/**
	 * The names of all the meta features that are computed by this characterizer
	 */
	protected String[] ids;

	/**
	 * The list of characterizers used in the computation of meta features
	 */
	protected ArrayList<Characterizer> characterizers;

	/**
	 * The names of the characterizers used
	 */
	protected Map<Characterizer, String> characterizerNames;

	/**
	 * The time it took to compute the meta features for each characterizer by name
	 */
	protected Map<String, Double> computationTimes = new HashMap<>();

	/**
	 * Initializes a new characterizer. Calls {@link #initializeCharacterizers()},
	 * {@link #initializeCharacterizerNames()} and
	 * {@link #initializeMetaFeatureIds()} in order.
	 * 
	 * @throws DatasetCharacterizerInitializationFailedException
	 *             if the characterizer cannot be initialized properly
	 */
	public GlobalCharacterizer() throws DatasetCharacterizerInitializationFailedException {
		logger.trace("Initialize");
		try {
			initializeCharacterizers();
		} catch (Exception e) {
			throw new DatasetCharacterizerInitializationFailedException(e);
		}
		initializeCharacterizerNames();
		initializeMetaFeatureIds();
	}

	@Override
	public Map<String, Double> characterize(Instances instances) {

		if (logger.isTraceEnabled()) {
			logger.trace("Characterize dataset \"{}\" ...", instances.relationName());
		}

		TreeMap<String, Double> metaFeatures = new TreeMap<>();
		StopWatch watch = new StopWatch();
		characterizers.forEach(characterizer -> {
			try {
				watch.reset();
				watch.start();
				metaFeatures.putAll(characterizer.characterize(instances));
				watch.stop();
				computationTimes.put(characterizer.toString(), (double) watch.getTime());
			} catch (Exception e) {
				for (String metaFeature : characterizer.getIDs()) {
					metaFeatures.put(metaFeature, Double.NaN);
				}
				computationTimes.put(characterizer.toString(), Double.NaN);
			}
		});

		logger.trace("Done characterizing dataset. Feature length: {}", metaFeatures.size());

		return metaFeatures;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		// Build String representation on the basis of the used characterizers
		for (Characterizer characterizer : characterizers) {
			builder.append(characterizer.toString());
			builder.append(System.lineSeparator());
			String[] baseCharacterizerIds = characterizer.getIDs();
			for (String id : baseCharacterizerIds) {
				builder.append(id + ",");
			}
			builder.append(System.lineSeparator());
		}

		return builder.toString();
	}

	/**
	 * Gets the list of characterizers used in the computation of meta features.
	 * 
	 * @return The characterizers
	 */
	public List<Characterizer> getCharacterizers() {
		return characterizers;
	}

	/**
	 * Gets the time in milliseconds it took to compute each group of meta features
	 * (Computed by a Characterizer). The computation times for the last time that
	 * {@link #characterize(Instances)} was called are returned. The time is NaN if
	 * the meta feature could not be computed.
	 * 
	 * @return The meta feature computation times
	 */
	public Map<String, Double> getMetaFeatureComputationTimes() {
		return computationTimes;
	}

	/**
	 * Gets the names of the used Characterizers.
	 * 
	 * @return The names of the characterizers
	 */
	public List<String> getCharacterizerNames() {
		List<String> names = new ArrayList<>();
		characterizerNames.values().forEach(names::add);
		return names;
	}

	/**
	 * Gets names for the used Characterizers.
	 * 
	 * @return The used Characterizers mapped to their names
	 */
	public Map<Characterizer, String> getCharacterizerNamesMappings() {
		return characterizerNames;
	}

	/**
	 * Gets the mapping of a Characterizer to the meta features it computes.
	 * 
	 * @return The mapping of Characterizer names to their meta features
	 */
	public Map<String, List<String>> getCharacterizerGroups() {
		Map<String, List<String>> results = new HashMap<>();
		characterizerNames.forEach((characterizer, name) -> results.put(name, Arrays.asList(characterizer.getIDs())));
		return results;
	}

	@Override
	public String[] getIDs() {
		return ids;
	}

	/**
	 * Adds the required characterizers to {@link #characterizers}.
	 * 
	 * @throws Exception
	 */
	protected void initializeCharacterizers() throws Exception {
		characterizers = new ArrayList<>();
		addNoProbingCharacterizers(characterizers);
		addLandmarkerCharacterizers(characterizers);
	}

	protected void addNoProbingCharacterizers(ArrayList<Characterizer> characterizerList) {
		characterizerList.addAll(Arrays.asList(new SimpleMetaFeatures(), new Statistical(),
		new NominalAttDistinctValues(), new Cardinality()));
	}

	protected void addLandmarkerCharacterizers(ArrayList<Characterizer> characterizerList) throws Exception {
		characterizerList.addAll(Arrays.asList(new GenericLandmarker("kNN1N", CP_IBK, 2, null),
				new GenericLandmarker("NaiveBayes", CP_NB, 2, null),
				new GenericLandmarker("DecisionStump", CP_DS, 2, null),
				new GenericLandmarker("CfsSubsetEval_kNN1N", CP_ASC, 2,
						Utils.splitOptions(PREPROCESSING_PREFIX + CP_IBK)),
				new GenericLandmarker("CfsSubsetEval_NaiveBayes", CP_ASC, 2,
						Utils.splitOptions(PREPROCESSING_PREFIX + CP_NB)),
				new GenericLandmarker("CfsSubsetEval_DecisionStump", CP_ASC, 2,
						Utils.splitOptions(PREPROCESSING_PREFIX + CP_DS))));
		
		StringBuilder zeroes = new StringBuilder();
		zeroes.append("0");
		for (int i = 1; i <= 3; ++i) {
			zeroes.append("0");
			String[] j48Option = { "-C", "." + zeroes.toString() + "1" };
			characterizerList
					.add(new GenericLandmarker("J48." + zeroes.toString() + "1.", "weka.classifiers.trees.J48", 2, j48Option));

			String[] repOption = { "-L", "" + i };
			characterizerList
					.add(new GenericLandmarker("REPTreeDepth" + i, "weka.classifiers.trees.REPTree", 2, repOption));

			String[] randomtreeOption = { "-depth", "" + i };
			characterizerList.add(new GenericLandmarker("RandomTreeDepth" + i, "weka.classifiers.trees.RandomTree", 2,
					randomtreeOption));
		}
	}
	
	/**
	 * Initializes {@link #characterizerNames}.
	 */
	protected void initializeCharacterizerNames() {
		characterizerNames = new HashMap<>();
		characterizers.forEach(characterizer -> {
			if (characterizer.getClass().equals(GenericLandmarker.class)) {
				String aUCName = characterizer.getIDs()[0];
				String name = aUCName.substring(0, aUCName.length() - 3);
				characterizerNames.put(characterizer, name);
			} else {
				characterizerNames.put(characterizer, characterizer.getClass().getSimpleName());
			}
		});
	}

	/**
	 * Initializes {@link #ids}.
	 */
	protected void initializeMetaFeatureIds() {
		List<String> metaFeatures = new ArrayList<>();
		for (Characterizer characterizer : characterizers) {
			for (String metaFeature : characterizer.getIDs()) {
				metaFeatures.add(metaFeature);
			}
		}

		ids = metaFeatures.toArray(new String[metaFeatures.size()]);
	}
}