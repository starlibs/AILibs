
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.Binarizer;
    /*
        threshold : float, optional (0.0 by default)
        Feature values below or equal to this are replaced by 0, above it by 1.
        Threshold may not be less than 0 for operations on sparse matrices.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_Binarizer_threshold extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 0.1;
        }

        @Override
        protected double getMax() {
            return 1;
        }

        @Override
        protected int getSteps() {
            return 3;
        }
        protected boolean isLinear() {
        	// TODO Auto-generated method stub
        	return false;
        }
        @Override
        protected boolean needsIntegers() {
            return false;
        }
    }
    
