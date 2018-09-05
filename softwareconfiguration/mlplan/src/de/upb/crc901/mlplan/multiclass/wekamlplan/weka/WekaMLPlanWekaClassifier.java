package de.upb.crc901.mlplan.multiclass.wekamlplan.weka;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.aeonbits.owner.ConfigFactory;

import de.upb.crc901.mlplan.multiclass.LossFunctionBuilder;
import de.upb.crc901.mlplan.multiclass.MLPlanClassifierConfig;
import de.upb.crc901.mlplan.multiclass.MultiClassPerformanceMeasure;
import de.upb.crc901.mlplan.multiclass.wekamlplan.MLPlanWekaBuilder;
import de.upb.crc901.mlplan.multiclass.wekamlplan.MLPlanWekaClassifier;
import hasco.serialization.ComponentLoader;
import jaicore.basic.FileUtil;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import weka.core.Instances;

public class WekaMLPlanWekaClassifier extends MLPlanWekaClassifier {
	
	static MLPlanClassifierConfig loadOwnerConfig(File configFile) throws IOException {
		Properties props = new Properties();
		FileInputStream fis = new FileInputStream(configFile);
		props.load(fis);
		return ConfigFactory.create(MLPlanClassifierConfig.class, props);
	}

	public WekaMLPlanWekaClassifier(MLPlanWekaBuilder builder) throws IOException {
		super(builder.getSearchSpaceConfigFile(), new WEKAPipelineFactory(), new LossFunctionBuilder().getEvaluator(builder.getPerformanceMeasure()), loadOwnerConfig(builder.getAlhorithmConfigFile()));
	}
	
	public WekaMLPlanWekaClassifier() throws IOException {
		super(new File("conf/automl/searchmodels/weka/weka-all-autoweka.json"), new WEKAPipelineFactory(), new LossFunctionBuilder().getEvaluator(MultiClassPerformanceMeasure.ERRORRATE), ConfigFactory.create(MLPlanClassifierConfig.class));
		
		PreferenceBasedNodeEvaluator preferenceNodeEvaluator = new PreferenceBasedNodeEvaluator(new ComponentLoader(getComponentFile()).getComponents(), FileUtil.readFileAsList(getConfig().preferredComponents()));
		this.setPreferredNodeEvaluator(preferenceNodeEvaluator);
	}

	@Override
	protected INodeEvaluator<TFDNode, Double> getSemanticNodeEvaluator(Instances data) {
		return new SemanticNodeEvaluator(getComponents(), data);
	}
}
