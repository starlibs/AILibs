/**
 * 
 */
package jaicore.ml.tsc.filter;

import java.util.ArrayList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.TimeSeriesDataset;
import jaicore.ml.core.dataset.attribute.IAttributeType;
import jaicore.ml.core.dataset.attribute.IAttributeValue;
import jaicore.ml.core.dataset.attribute.timeseries.TimeSeriesAttributeType;
import jaicore.ml.core.dataset.attribute.timeseries.TimeSeriesAttributeValue;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;


/**
 * @author Helen
 *
 */
public class ZTransformer implements IFilter {
	
	private INDArray means;
	private INDArray deviation;
	private boolean fitted = false;

	
	//TODO alle methoden static ? 
	
	/* (non-Javadoc)
	 * @see jaicore.ml.tsc.filter.IFilter#transform(jaicore.ml.core.dataset.IDataset)
	 */
	@Override
	public IDataset transform(IDataset input) throws IllegalArgumentException, NoneFittedFilterExeception{
		
		if(input.isEmpty()) {
			throw new IllegalArgumentException("The input dataset was empty");
		}
		
		if(!(input instanceof TimeSeriesDataset)) {
			throw new IllegalArgumentException("This method only works with timeseries datasets");
		}
		if(fitted) {
			List<IAttributeType<?>> ListOfAttributes = input.getAttributeTypes();
			int numberOfTSAttributes = 0;
			
			for(int i = 0; i < input.getNumberOfAttributes(); i++) {
				if(ListOfAttributes.get(i) instanceof TimeSeriesAttributeType) {
					
					numberOfTSAttributes++;
					
					 INDArray matrix =  ((TimeSeriesDataset) input).getMatrixForAttributeType((TimeSeriesAttributeType)ListOfAttributes.get(i));
					 for(int instance = 0; instance < input.size(); instance++) {
						INDArray row =  matrix.getRow(instance);
						for(int elem = 0; elem < row.length(); elem++) {
							//update every elem by the calculation of elem multiplied by the mean of the according instance
							row.putScalar(elem, ((row.getDouble(new int[]{instance,elem})*means.getDouble(new int [] {numberOfTSAttributes,instance}))/deviation.getDouble(new int[]{numberOfTSAttributes,instance})));
						}
					 }
				}
			}
		}else {
			throw new NoneFittedFilterExeception("The fit method must be called before the transform method.");
		}
		
		return input;
	}

	/* (non-Javadoc)
	 * @see jaicore.ml.tsc.filter.IFilter#fit(jaicore.ml.core.dataset.IDataset)
	 */
	
	@Override
	public void fit(IDataset input) {
		List<IAttributeType<?>> listOfAttributeTypes = input.getAttributeTypes();
		int numberOfTSAttributeTypes = 0;
		ArrayList<INDArray> ListOfToTransformedMatrices = new ArrayList<INDArray>();
		
		
		for(IAttributeType<?> type: listOfAttributeTypes){
			if(type instanceof TimeSeriesAttributeType) {
				numberOfTSAttributeTypes++;
				ListOfToTransformedMatrices.add(((TimeSeriesDataset) input).getMatrixForAttributeType((TimeSeriesAttributeType)type));
			}
		}
		
		means = Nd4j.zeros(numberOfTSAttributeTypes, input.size());
		deviation = Nd4j.zeros(numberOfTSAttributeTypes, input.size());
		
		for(int matrix = 0; matrix < ListOfToTransformedMatrices.size(); matrix++) {
			for(int row = 0; row< ListOfToTransformedMatrices.get(matrix).size(0);row++) {
				int [] index = {matrix, row};
				means.putScalar(index,(double)ListOfToTransformedMatrices.get(matrix).getRow(row).mean(1).getDouble(0));
				deviation.putScalar(index, (double)ListOfToTransformedMatrices.get(matrix).getRow(row).std(1).getDouble(0));
			}		
		}
		
		fitted = true;
	}


	/* (non-Javadoc)
	 * @see jaicore.ml.tsc.filter.IFilter#fitTransform(jaicore.ml.core.dataset.IDataset)
	 */
	@Override
	public IDataset fitTransform(IDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		//TODO call fit in transform or not ?
		fit(input);
		fitted = true;
		return transform(input);
	}

}
