
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.GraphLasso;
    /*
        max_iter : integer, default 100
        The maximum number of iterations.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_GraphLasso_max_iter extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 10;
        }

        @Override
        protected double getMax() {
            return 500;
        }

        @Override
        protected int getSteps() {
            return 4;
        }

        @Override
        protected boolean needsIntegers() {
            return true;
        }
    }
    
