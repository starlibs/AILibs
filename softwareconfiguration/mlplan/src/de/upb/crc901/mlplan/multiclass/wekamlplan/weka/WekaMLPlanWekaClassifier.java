package de.upb.crc901.mlplan.multiclass.wekamlplan.weka;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.aeonbits.owner.ConfigFactory;

import de.upb.crc901.mlpipeline_evaluation.CacheEvaluatorMeasureBridge;
import de.upb.crc901.mlplan.dyadranking.DyadRankingBasedNodeEvaluator;
import de.upb.crc901.mlplan.multiclass.MLPlanClassifierConfig;
import de.upb.crc901.mlplan.multiclass.wekamlplan.MLPlanWekaBuilder;
import de.upb.crc901.mlplan.multiclass.wekamlplan.MLPlanWekaClassifier;
import hasco.serialization.ComponentLoader;
import jaicore.basic.FileUtil;
import jaicore.ml.core.evaluation.measure.ADecomposableDoubleMeasure;
import jaicore.ml.core.evaluation.measure.singlelabel.MultiClassMeasureBuilder;
import jaicore.ml.evaluation.evaluators.weka.AbstractEvaluatorMeasureBridge;
import jaicore.ml.evaluation.evaluators.weka.SimpleEvaluatorMeasureBridge;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import weka.core.Instances;

public class WekaMLPlanWekaClassifier extends MLPlanWekaClassifier {

	static MLPlanClassifierConfig loadOwnerConfig(File configFile) throws IOException {
		Properties props = new Properties();
		if (configFile.exists()) {
			FileInputStream fis = new FileInputStream(configFile);
			props.load(fis);
		} else
			System.out.println(
					"Config file " + configFile.getAbsolutePath() + " not found, working with default parameters.");
		return ConfigFactory.create(MLPlanClassifierConfig.class, props);
	}

	public WekaMLPlanWekaClassifier(MLPlanWekaBuilder builder) throws IOException {
		super(builder.getSearchSpaceConfigFile(), new WEKAPipelineFactory(), getBridge(builder),
				builder.getAlhorithmConfigFile() != null ? loadOwnerConfig(builder.getAlhorithmConfigFile())
						: ConfigFactory.create(MLPlanClassifierConfig.class));
		INodeEvaluator<TFDNode, Double> preferenceNodeEvaluator;
		if (this.getConfig().getNodeEvaluator().equals("dyadrankingbased")) {
			preferenceNodeEvaluator = new DyadRankingBasedNodeEvaluator<>(
					new ComponentLoader(getComponentFile()));

		} else {
			preferenceNodeEvaluator = new PreferenceBasedNodeEvaluator(
					new ComponentLoader(getComponentFile()).getComponents(),
					FileUtil.readFileAsList(getConfig().preferredComponents()));
		}
		this.setPreferredNodeEvaluator(preferenceNodeEvaluator);
	}

	private static AbstractEvaluatorMeasureBridge<Double, Double> getBridge(MLPlanWekaBuilder builder) {
		ADecomposableDoubleMeasure<Double> measure = new MultiClassMeasureBuilder()
				.getEvaluator(builder.getPerformanceMeasure());

		if (builder.getUseCache()) {
			return new CacheEvaluatorMeasureBridge(measure, builder.getDBAdapter());
		} else {
			return new SimpleEvaluatorMeasureBridge(measure);
		}

	}

	public WekaMLPlanWekaClassifier() throws IOException {
		this(new MLPlanWekaBuilder());

	}

	@Override
	protected INodeEvaluator<TFDNode, Double> getSemanticNodeEvaluator(Instances data) {
		return new SemanticNodeEvaluator(getComponents(), data);
	}
}
