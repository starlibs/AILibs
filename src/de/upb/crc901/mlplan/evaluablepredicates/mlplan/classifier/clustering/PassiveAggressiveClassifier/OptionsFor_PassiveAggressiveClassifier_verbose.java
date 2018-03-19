
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.clustering.PassiveAggressiveClassifier;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        verbose : integer, optional
        The verbosity level


    */
    public class OptionsFor_PassiveAggressiveClassifier_verbose extends NumericRangeOptionPredicate {
        
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
    
