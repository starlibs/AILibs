
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.FactorAnalysis;
    /*
        random_state : int, RandomState instance or None, optional (default=0)
        If int, random_state is the seed used by the random number generator;
        If RandomState instance, random_state is the random number generator;
        If None, the random number generator is the RandomState instance used
        by `np.random`. Only used when ``svd_method`` equals 'randomized'.

    Attributes
    
    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_FactorAnalysis_random_state extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 1;
        }

        @Override
        protected double getMax() {
            return 1000000;
        }

        @Override
        protected int getSteps() {
            return 500;
        }

        @Override
        protected boolean needsIntegers() {
            return true;
        }
    }
    
