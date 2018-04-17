
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.clustering.LogisticRegression;

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        verbose : int, default: 0
        For the liblinear and lbfgs solvers set verbose to any positive
        number for verbosity.


    */
    public class OptionsFor_LogisticRegression_verbose extends NumericRangeOptionPredicate {
        
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
    
