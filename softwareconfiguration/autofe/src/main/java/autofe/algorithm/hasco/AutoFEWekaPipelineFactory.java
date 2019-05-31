package autofe.algorithm.hasco;

import ai.libs.hasco.exceptions.ComponentInstantiationFailedException;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hasco.optimizingfactory.BaseFactory;
import ai.libs.mlplan.multiclass.wekamlplan.weka.WEKAPipelineFactory;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.algorithm.hasco.filter.meta.FilterPipelineFactory;
import weka.classifiers.Classifier;

public class AutoFEWekaPipelineFactory implements BaseFactory<AutoFEWekaPipeline> {

	private FilterPipelineFactory filterPipelineFactory;
	private final WEKAPipelineFactory wekaPipelineFactory;

	public AutoFEWekaPipelineFactory(final FilterPipelineFactory filterPipelineFactory,
			final WEKAPipelineFactory wekaPipelineFactory) {
		this.filterPipelineFactory = filterPipelineFactory;
		this.wekaPipelineFactory = wekaPipelineFactory;
	}

	@Override
	public AutoFEWekaPipeline getComponentInstantiation(final ComponentInstance groundComponent) throws ComponentInstantiationFailedException {
		if (groundComponent == null) {
			return null;
		}

		ComponentInstance filterPipelineInstance = groundComponent.getSatisfactionOfRequiredInterfaces()
				.get("filterPipeline");
		ComponentInstance wekaPipelineInstance = groundComponent.getSatisfactionOfRequiredInterfaces()
				.get("mlPipeline");

		FilterPipeline filterPipeline = null;
		if (filterPipelineInstance != null) {
			filterPipeline = filterPipelineFactory.getComponentInstantiation(filterPipelineInstance);
		}

		Classifier mlPipeline = null;
		if (wekaPipelineInstance != null) {
			try {
				mlPipeline = wekaPipelineFactory.getComponentInstantiation(wekaPipelineInstance);
			} catch (IllegalArgumentException e) {
				// XXX the pipeline specification might be partial.
			}
		}

		return new AutoFEWekaPipeline(filterPipeline, mlPipeline);
	}

}
