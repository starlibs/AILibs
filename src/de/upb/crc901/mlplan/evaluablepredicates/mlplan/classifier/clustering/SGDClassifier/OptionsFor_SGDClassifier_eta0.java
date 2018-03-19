
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.clustering.SGDClassifier;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        eta0 : double
        The initial learning rate for the 'constant' or 'invscaling'
        schedules. The default value is 0.0 as eta0 is not used by the
        default schedule 'optimal'.


    */
    public class OptionsFor_SGDClassifier_eta0 extends NumericRangeOptionPredicate {
        
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
    
