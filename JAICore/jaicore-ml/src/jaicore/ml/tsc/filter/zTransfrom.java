/**
 * 
 */
package jaicore.ml.tsc.filter;

import jaicore.ml.core.dataset.IDataset;

/**
 * @author Helen
 *
 */
public class zTransfrom implements IFilter {

	/* (non-Javadoc)
	 * @see jaicore.ml.tsc.filter.IFilter#transform(jaicore.ml.core.dataset.IDataset)
	 */
	@Override
	public IDataset transform(IDataset input) throws IllegalArgumentException{
		if(input.isEmpty()) {
			throw new IllegalArgumentException("The input dataset was empty");
		}
		
		int length = input.getAttributeTypes().size();
		for( int i = 0; i< length; i++) {
			
		if(true){
				
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see jaicore.ml.tsc.filter.IFilter#fit(jaicore.ml.core.dataset.IDataset)
	 */
	@Override
	public void fit(IDataset input) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see jaicore.ml.tsc.filter.IFilter#fitTransform(jaicore.ml.core.dataset.IDataset)
	 */
	@Override
	public IDataset fitTransform(IDataset input) {
		// TODO Auto-generated method stub
		return null;
	}

}
