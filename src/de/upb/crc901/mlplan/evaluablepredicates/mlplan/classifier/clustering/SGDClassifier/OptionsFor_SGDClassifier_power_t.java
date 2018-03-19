
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.clustering.SGDClassifier;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        power_t : double
        The exponent for inverse scaling learning rate [default 0.5].


    */
    public class OptionsFor_SGDClassifier_power_t extends NumericRangeOptionPredicate {
        
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
    
