
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.MDS;
    /*
        max_iter : int, optional, default: 300
        Maximum number of iterations of the SMACOF algorithm for a single run.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_MDS_max_iter extends NumericRangeOptionPredicate {
        
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
    
