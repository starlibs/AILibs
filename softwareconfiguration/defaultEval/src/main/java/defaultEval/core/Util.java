package defaultEval.core;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.upb.crc901.automl.hascocombinedml.MLServicePipelineFactory;
import de.upb.crc901.automl.pipeline.service.MLServicePipeline;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.serialization.ComponentLoader;
import scala.util.parsing.combinator.testing.Str;

public class Util {
	//TODO remove absolute Path
	
	public static void loadClassifierComponents(ComponentLoader cl) throws IOException {
		cl.loadComponents(new File("F:\\Data\\Uni\\PG\\AILibs\\softwareconfiguration\\mlplan\\model\\weka\\weka-classifiers-autoweka.json"));
	}
	
	public static void loadPreprocessorComponents(ComponentLoader cl) throws IOException {
		cl.loadComponents(new File("F:\\Data\\Uni\\PG\\AILibs\\softwareconfiguration\\mlplan\\model\\weka\\weka-preprocessors-autoweka.json"));
	}
	
	
	
	public static MLServicePipeline createPipeline(Component prePorcessor, Map<String, String> prePorcessorParameter, Component classifier, Map<String, String> classifierParameter) {
		
		MLServicePipeline mlPipeline = null;
		
		if(prePorcessor != null) {
			ComponentInstance preProcessorInstance = new ComponentInstance(prePorcessor, prePorcessorParameter, new HashMap<>());
			ComponentInstance classifierInstance = new ComponentInstance(classifier, classifierParameter, new HashMap<>());	// TODO req.!
			
			Map<String, ComponentInstance> satisfactionOfRequiredInterfaces = new HashMap<>();
			satisfactionOfRequiredInterfaces.put("preprocessor", preProcessorInstance);
			satisfactionOfRequiredInterfaces.put("classifier", classifierInstance);
			
			ComponentInstance pipelineInstance = new ComponentInstance(new Component("pipeline"), new HashMap<>(), satisfactionOfRequiredInterfaces);
			
			MLServicePipelineFactory pipilineFactory = new MLServicePipelineFactory();
			mlPipeline = pipilineFactory.getComponentInstantiation(pipelineInstance);
			
		}else {
			ComponentInstance classifierInstance = new ComponentInstance(classifier, classifierParameter, new HashMap<>());	// TODO req.!
			
			MLServicePipelineFactory pipilineFactory = new MLServicePipelineFactory();
			mlPipeline = pipilineFactory.getComponentInstantiation(classifierInstance);
		}
		
		return mlPipeline;
		
	}
	

}
