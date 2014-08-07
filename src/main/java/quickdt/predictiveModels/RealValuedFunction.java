package quickdt.predictiveModels;

import quickdt.data.AbstractInstance;

/**
 * Created by alexanderhawk on 7/29/14.
 */
public abstract class RealValuedFunction extends AbstractPredictiveModel<Double> implements PredictiveModel<Double> {
    public abstract Double predict(Double regressor);
}
