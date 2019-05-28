package jaicore.ml.ranking.clusterbased.modifiedisac;

import java.util.Map;

import jaicore.ml.metafeatures.DatasetCharacterizerInitializationFailedException;
import jaicore.ml.metafeatures.GlobalCharacterizer;
import jaicore.ml.ranking.clusterbased.customdatatypes.ProblemInstance;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * @author Helen
 *	takes an instances object as input and calculates its meta feature via @author Helena Grafs bachelor thesis
 *	the found meta feature are than translated in the formated needed by modified ISAC.
 */
public class HellFormater {

	private HellFormater() {
		/* do nothing */
	}

	/**
	 * @param input the instances object whose meta feature are to be computed
	 * @return the formated and computed meta feature.
	 * @throws DatasetCharacterizerInitializationFailedException
	 */
	public static ProblemInstance<Instance> formate(final Instances input) throws DatasetCharacterizerInitializationFailedException {
		GlobalCharacterizer chara = new GlobalCharacterizer();

		// finding the meta feature via the bachelor thesis.
		return formatInstance(chara.characterize(input));
	}

	/**
	 * @param toFormatInstance The instances meta feature.
	 * @return The porbleminstance formed by the meta feature.
	 */
	private static ProblemInstance<Instance> formatInstance(final Map<String, Double> toFormatInstance) {
		double[] collectedAttributes = new double[ModifiedISACInstanceCollector.getAtributesofTrainingsdata().size()];
		// takes the found Attributes of the used instance collector
		for (int i = 0; i < ModifiedISACInstanceCollector.getAtributesofTrainingsdata().size(); i++) {
			collectedAttributes[i] = toFormatInstance.get(ModifiedISACInstanceCollector.getAtributesofTrainingsdata().get(i));
		}
		// creates a problem instance out of the resulting dense instance.
		DenseInstance inst = new DenseInstance(1,collectedAttributes);
		return new ProblemInstance<>(inst);
	}
}
