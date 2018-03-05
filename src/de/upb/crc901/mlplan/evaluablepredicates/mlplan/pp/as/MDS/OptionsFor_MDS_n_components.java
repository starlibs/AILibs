
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.MDS;
    /*
        n_components : int, optional, default: 2
        Number of dimensions in which to immerse the dissimilarities.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_MDS_n_components extends NumericRangeOptionPredicate {
        
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
    
