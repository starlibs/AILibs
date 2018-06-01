
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.GraphLassoCV;
    /*
        n_refinements : strictly positive integer
        The number of times the grid is refined. Not used if explicit
        values of alphas are passed.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_GraphLassoCV_n_refinements extends NumericRangeOptionPredicate {
        
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
    
