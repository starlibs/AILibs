
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.clustering.LogisticRegressionCV;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        Cs : list of floats | int
        Each of the values in Cs describes the inverse of regularization
        strength. If Cs is as an int, then a grid of Cs values are chosen
        in a logarithmic scale between 1e-4 and 1e4.
        Like in support vector machines, smaller values specify stronger
        regularization.


    */
    public class OptionsFor_LogisticRegressionCV_Cs extends NumericRangeOptionPredicate {
        
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
            return true; // already set by generator
        }

        @Override
        protected boolean isLinear() {
			return true;
		}
    }
    
