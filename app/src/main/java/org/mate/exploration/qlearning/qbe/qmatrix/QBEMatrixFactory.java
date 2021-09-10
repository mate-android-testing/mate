package org.mate.exploration.qlearning.qbe.qmatrix;

import android.os.Build;
import android.support.annotation.RequiresApi;

import org.mate.exploration.qlearning.qbe.interfaces.implementations.QBEAction;
import org.mate.exploration.qlearning.qbe.interfaces.implementations.QBEState;

/**
 * Values of the QMatrixes taken from
 * Y. Koroglu et al., "QBE: QLearning-Based Exploration of Android Applications,"
 * 2018 IEEE 11th International Conference on Software Testing, Verification and Validation (ICST),
 * 2018, pp. 105-115, doi: 10.1109/ICST.2018.00020.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public final class QBEMatrixFactory {

    private static final double[] activityCoverage = {
            0.11, 0.09, 0.40, 0.00, 0.10, 0.30, 0.00,
            0.13, 0.44, 0.26, 0.00, 0.12, 0.05, 0.00,
            0.06, 0.66, 0.16, 0.00, 0.13, 0.00, 0.00,
            0.17, 0.25, 0.40, 0.00, 0.09, 0.09, 0.00,
            0.06, 0.28, 0.52, 0.00, 0.09, 0.05, 0.00,
    };

    private static final double[] numberOfCrashes = {
            0.04, 0.18, 0.33, 0.00, 0.12, 0.33, 0.00,
            0.19, 0.18, 0.12, 0.00, 0.44, 0.07, 0.00,
            0.13, 0.43, 0.15, 0.00, 0.07, 0.23, 0.00,
            0.17, 0.18, 0.48, 0.00, 0.18, 0.00, 0.00,
            0.33, 0.26, 0.13, 0.00, 0.23, 0.04, 0.00,
    };

    public QMatrix<QBEState, QBEAction> getMaximizeActivityCoverageQMatrix() {
        return new QMatrix<>(new QBEAbstractState(), new QBEAbstractAction(), activityCoverage);
    }


    public QMatrix<QBEState, QBEAction> getMaximizesNumberOfCrashesQMatrix() {
        return new QMatrix<>(new QBEAbstractState(), new QBEAbstractAction(), numberOfCrashes);
    }
}
