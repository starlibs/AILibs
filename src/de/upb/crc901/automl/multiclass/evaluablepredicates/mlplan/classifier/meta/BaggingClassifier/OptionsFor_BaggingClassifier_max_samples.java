
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.meta.BaggingClassifier;
    /*
        max_samples : int or float, optional (default=1.0)
        The number of samples to draw from X to train each base estimator.
            - If int, then draw `max_samples` samples.
            - If float, then draw `max_samples * X.shape[0]` samples.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_BaggingClassifier_max_samples extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return .1;
        }

        @Override
        protected double getMax() { // 1 is covered by default
            return .8;
        }

        @Override
        protected int getSteps() {
            return 4;
        }

        @Override
        protected boolean needsIntegers() {
            return false;
        }
    }
    
