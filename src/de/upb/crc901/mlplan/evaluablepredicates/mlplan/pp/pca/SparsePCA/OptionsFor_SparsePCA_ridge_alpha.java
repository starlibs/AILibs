
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.pca.SparsePCA;
    /*
        ridge_alpha : float,
        Amount of ridge shrinkage to apply in order to improve
        conditioning when calling the transform method.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_SparsePCA_ridge_alpha extends NumericRangeOptionPredicate {
        
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
    
