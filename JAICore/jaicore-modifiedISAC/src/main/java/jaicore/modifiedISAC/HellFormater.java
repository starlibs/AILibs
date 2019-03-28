package jaicore.modifiedISAC;

import java.util.Map;

import jaicore.CustomDataTypes.ProblemInstance;
import jaicore.ml.metafeatures.GlobalCharacterizer;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * @author Helen
 *	takes an instances object as input and calculates its meta feature via @author Helena Grafs bachelor thesis
 *	the found meta feature are than translated in the formated needed by modified ISAC.  
 */
public class HellFormater {

	/**
	 * @param input the instances object whose meta feature are to be computed 
	 * @return the formated and computed meta feature.
	 */
	public static ProblemInstance<Instance> formate(Instances input) {
		GlobalCharacterizer chara = null;
		try {
			chara = new GlobalCharacterizer();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// finding the meta feature via the bachelor thesis. 
		Map<String, Double> toFormat = chara.characterize(input);

		return formatInstance(toFormat);
	}

	/**
	 * @param toFormatInstance The instances meta feature.
	 * @return The porbleminstance formed by the meta feature.
	 */
	private static ProblemInstance<Instance> formatInstance(Map<String, Double> toFormatInstance) {
		double[] collectedAttributes = new double[ModifiedISACInstanceCollector.getAtributesofTrainingsdata().size()];
		// takes the found Attributes of the used instance collector
		for (int i = 0; i < ModifiedISACInstanceCollector.getAtributesofTrainingsdata().size(); i++) {
			collectedAttributes[i] = toFormatInstance.get(ModifiedISACInstanceCollector.getAtributesofTrainingsdata().get(i));
		}
		// creates a problem instance out of the resulting dense instance.
		DenseInstance inst = new DenseInstance(1,collectedAttributes);
		ProblemInstance<Instance> formated = new ProblemInstance<Instance>(inst);
		return formated;
	}
}
