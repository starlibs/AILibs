
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.Isomap;
    /*
        n_components : integer
        number of coordinates for the manifold


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_Isomap_n_components extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 1;
        }

        @Override
        protected double getMax() {
            return 10;
        }

        @Override
        protected int getSteps() {
            return 3;
        }

        @Override
        protected boolean needsIntegers() {
            return true;
        }
    }
    
