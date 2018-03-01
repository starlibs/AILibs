
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.BayesianGaussianMixture;
    /*
        max_iter : int, defaults to 100.
        The number of EM iterations to perform.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_BayesianGaussianMixture_max_iter extends NumericRangeOptionPredicate {
        
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
            return true;
        }
    }
    
