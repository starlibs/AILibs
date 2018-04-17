
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.MLPClassifier;
    /*
        tol : float, optional, default 1e-4
        Tolerance for the optimization. When the loss or score is not improving
        by at least tol for two consecutive iterations, unless `learning_rate`
        is set to 'adaptive', convergence is considered to be reached and
        training stops.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_MLPClassifier_tol extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 1;
        }

        @Override
        protected double getMax() {
            return 1;
        }

        @Override
        protected int getSteps() {
            return -1;
        }

        @Override
        protected boolean needsIntegers() {
            return false;
        }
    }
    
