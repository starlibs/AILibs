
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.NuSVC;
    /*
        nu : float, optional (default=0.5)
        An upper bound on the fraction of training errors and a lower
        bound of the fraction of support vectors. Should be in the
        interval (0, 1].


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_NuSVC_nu extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 0.01;
        }

        @Override
        protected double getMax() {
            return 1;
        }

        @Override
        protected int getSteps() {
            return 5;
        }

        @Override
        protected boolean needsIntegers() {
            return false;
        }
    }
    
