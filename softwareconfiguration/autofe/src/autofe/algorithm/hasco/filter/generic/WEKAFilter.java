package autofe.algorithm.hasco.filter.generic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.meta.IFilter;
import autofe.util.DataSet;
import weka.filters.Filter;

/**
 * Filter containing WEKA Filter objects.
 * 
 * @author Julian Lienen
 *
 */
public class WEKAFilter implements IFilter {

	private static Logger logger = LoggerFactory.getLogger(WEKAFilter.class);

	private Filter wekaFilter;

	public WEKAFilter(final Filter wekaFilter) {
		this.wekaFilter = wekaFilter;
	}

	@Override
	public DataSet applyFilter(DataSet inputData, boolean copy) {

		inputData.updateInstances();

		if (copy)
			inputData = inputData.copy();

		try {
			inputData.setInstances(Filter.useFilter(inputData.getInstances(), this.wekaFilter));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.warn("Could not apply weka filter. Reason: " + e.getMessage());
			e.printStackTrace();
		}

		return inputData;
	}

	@Override
	public String toString() {
		if (wekaFilter != null)
			return "WEKAFilter [wekaFilter=" + wekaFilter.getClass().getSimpleName() + "]";
		else
			return "WEKAFilter [wekaFilter=" + wekaFilter + "]";
	}

}
