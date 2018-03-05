
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.Imputer;
    /*
        verbose : integer, optional (default=0)
        Controls the verbosity of the imputer.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_Imputer_verbose extends NumericRangeOptionPredicate {
        
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
    
