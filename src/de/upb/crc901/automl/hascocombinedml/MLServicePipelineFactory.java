package de.upb.crc901.automl.hascocombinedml;

import de.upb.crc901.automl.pipeline.service.MLPipelinePlan;
import de.upb.crc901.automl.pipeline.service.MLPipelinePlan.MLPipe;
import de.upb.crc901.automl.pipeline.service.MLServicePipeline;

import java.util.Map;

import org.aeonbits.owner.ConfigCache;

import hasco.model.ComponentInstance;
import hasco.query.Factory;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;

public class MLServicePipelineFactory implements Factory<MLServicePipeline> {
  private static final HASCOForCombinedMLConfig CONFIG = ConfigCache.getOrCreate(HASCOForCombinedMLConfig.class);

  @Override
  public MLServicePipeline getComponentInstantiation(final ComponentInstance groundComponent) {

    MLPipelinePlan plan = new MLPipelinePlan();

    try {
      switch (groundComponent.getComponent().getName()) {
        case "pipeline": {
          ComponentInstance preprocessorCI = groundComponent.getSatisfactionOfRequiredInterfaces().get("preprocessor");
          if (preprocessorCI.getComponent().getName().startsWith("sklearn")) {
            MLPipe preprocessorPipe = plan.addAttributeSelection(preprocessorCI.getComponent().getName());
            this.setParameters(plan, preprocessorPipe, preprocessorCI.getParameterValues());
          }

          ComponentInstance classifierCI = groundComponent.getSatisfactionOfRequiredInterfaces().get("classifier");
          if (classifierCI.getComponent().getName().startsWith("sklearn")) {
            MLPipe classifierPipe = plan.setClassifier(classifierCI.getComponent().getName());
            this.setParameters(plan, classifierPipe, classifierCI.getParameterValues());
          }
          break;
        }
        default: {
          if (groundComponent.getComponent().getName().startsWith("sklearn")) {
            MLPipe classifierPipe = plan.setClassifier(groundComponent.getComponent().getName());
            this.setParameters(plan, classifierPipe, groundComponent.getParameterValues());
          } else {
            Classifier c = AbstractClassifier.forName(groundComponent.getComponent().getName(), null);
            plan.setClassifier(c);
          }
          break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      return new MLServicePipeline(plan);
    } catch (InterruptedException e) {
      return null;
    }
  }

  private void setParameters(final MLPipelinePlan plan, final MLPipe pipe, final Map<String, String> parameterValues) {
    for (String parameterName : parameterValues.keySet()) {
      plan.addOptions(pipe, parameterName, parameterValues.get(parameterName));
    }
  }

}
