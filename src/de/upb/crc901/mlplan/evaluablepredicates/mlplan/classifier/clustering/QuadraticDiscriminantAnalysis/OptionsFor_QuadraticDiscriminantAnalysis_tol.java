
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.clustering.QuadraticDiscriminantAnalysis;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        tol : float, optional, default 1.0e-4
        Threshold used for rank estimation.

        .. versionadded:: 0.17

    Attributes
    
    */
    public class OptionsFor_QuadraticDiscriminantAnalysis_tol extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 1
                ;
        }

        @Override
        protected double getMax() {
            return 1
                ;
        }

        @Override
        protected int getSteps() {
            return -1
                ;
        }

        @Override
        protected boolean needsIntegers() {
            return false; // already set by generator
        }

        @Override
        protected boolean isLinear() {
			return true;
		}
    }
    
