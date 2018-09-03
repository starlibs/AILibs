package de.upb.crc901.mlplan.multiclass.wekamlplan.weka;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.aeonbits.owner.ConfigFactory;

import de.upb.crc901.mlplan.multiclass.LossFunctionBuilder;
import de.upb.crc901.mlplan.multiclass.MLPlanClassifierConfig;
import de.upb.crc901.mlplan.multiclass.MultiClassPerformanceMeasure;
import de.upb.crc901.mlplan.multiclass.wekamlplan.WekaMLPlanBuilder;
import de.upb.crc901.mlplan.multiclass.wekamlplan.WekaMLPlanClassifier;
import hasco.serialization.ComponentLoader;
import jaicore.basic.FileUtil;

public class WekaMLPlanWekaClassifier extends WekaMLPlanClassifier {
	
	static MLPlanClassifierConfig loadOwnerConfig(File configFile) throws IOException {
		Properties props = new Properties();
		FileInputStream fis = new FileInputStream(configFile);
		props.load(fis);
		return ConfigFactory.create(MLPlanClassifierConfig.class, props);
	}

	public WekaMLPlanWekaClassifier(WekaMLPlanBuilder builder) throws IOException {
		super(builder.getSearchSpaceConfigFile(), new WEKAPipelineFactory(), new LossFunctionBuilder().getEvaluator(builder.getPerformanceMeasure()), loadOwnerConfig(builder.getAlhorithmConfigFile()));
	}
	
	public WekaMLPlanWekaClassifier() throws IOException {
		super(new File("conf/automl/searchmodels/weka/autoweka.json"), new WEKAPipelineFactory(), new LossFunctionBuilder().getEvaluator(MultiClassPerformanceMeasure.ERRORRATE), ConfigFactory.create(MLPlanClassifierConfig.class));
		this.setPreferredNodeEvaluator(new PreferenceBasedNodeEvaluator(new ComponentLoader(getComponentFile()).getComponents(), FileUtil.readFileAsList(getConfig().preferredComponents())));
	}
}
