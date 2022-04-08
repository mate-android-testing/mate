package org.mate.utils.coverage;

import androidx.annotation.NonNull;

import org.mate.Properties;

/**
 * A dto to transmit coverage information.
 */
public class CoverageDTO {

    /**
     * The current activity coverage or {@code null} if not specified.
     */
    private Double activityCoverage;

    /**
     * The current method coverage or {@code null} if not specified.
     */
    private Double methodCoverage;

    /**
     * The current branch coverage or {@code null} if not specified.
     */
    private Double branchCoverage;

    /**
     * The current line coverage or {@code null} if not specified.
     */
    private Double lineCoverage;

    /**
     * Specifies the activity coverage.
     *
     * @param activityCoverage The new activity coverage.
     */
    public void setActivityCoverage(double activityCoverage) {
        this.activityCoverage = activityCoverage;
    }

    /**
     * Returns the activity coverage.
     *
     * @return Returns the activity coverage.
     */
    public double getActivityCoverage() {
        return activityCoverage;
    }

    /**
     * Returns the method coverage.
     *
     * @return Returns the method coverage.
     */
    public double getMethodCoverage() {
        return methodCoverage;
    }

    /**
     * Specifies the method coverage.
     *
     * @param methodCoverage The new method coverage.
     */
    public void setMethodCoverage(double methodCoverage) {
        this.methodCoverage = methodCoverage;
    }

    /**
     * Returns the branch coverage.
     *
     * @return Returns the branch coverage.
     */
    public double getBranchCoverage() {
        return branchCoverage;
    }

    /**
     * Specifies the branch coverage.
     *
     * @param branchCoverage The new branch coverage.
     */
    public void setBranchCoverage(double branchCoverage) {
        this.branchCoverage = branchCoverage;
    }

    /**
     * Returns the line coverage.
     *
     * @return Returns the line coverage.
     */
    public double getLineCoverage() {
        return lineCoverage;
    }

    /**
     * Specifies the line coverage.
     *
     * @param lineCoverage The new line coverage.
     */
    public void setLineCoverage(double lineCoverage) {
        this.lineCoverage = lineCoverage;
    }

    /**
     * Returns the raw coverage information.
     *
     * @return Returns the raw coverage information depending on the specified coverage criterion.
     */
    public double getCoverage(Coverage coverage) {

        switch (coverage) {
            case ACTIVITY_COVERAGE:
                return getActivityCoverage();
            case METHOD_COVERAGE:
                return getMethodCoverage();
            case BRANCH_COVERAGE:
            case BASIC_BLOCK_BRANCH_COVERAGE:
                return getBranchCoverage();
            case LINE_COVERAGE:
            case BASIC_BLOCK_LINE_COVERAGE:
            case ALL_COVERAGE:
                return getLineCoverage();
            default:
                throw new UnsupportedOperationException("Coverage type "
                        + Properties.COVERAGE() + " not yet supported!");
        }
    }

    /**
     * Returns a dummy coverage dto. This is intended to be used with dummy {@link org.mate.model.TestCase}s.
     *
     * @param coverage The specified coverage criterion.
     * @return Returns the dummy coverage dto.
     */
    public static CoverageDTO getDummyCoverageDTO(Coverage coverage) {

        CoverageDTO coverageDTO = new CoverageDTO();

        switch (coverage) {
            case ACTIVITY_COVERAGE:
                coverageDTO.setActivityCoverage(0.0);
                break;
            case METHOD_COVERAGE:
                coverageDTO.setMethodCoverage(0.0);
                break;
            case BASIC_BLOCK_BRANCH_COVERAGE:
            case BRANCH_COVERAGE:
                coverageDTO.setBranchCoverage(0.0);
                break;
            case BASIC_BLOCK_LINE_COVERAGE:
            case LINE_COVERAGE:
                coverageDTO.setLineCoverage(0.0);
                break;
            case ALL_COVERAGE:
                coverageDTO.setActivityCoverage(0.0);
                coverageDTO.setMethodCoverage(0.0);
                coverageDTO.setBranchCoverage(0.0);
                coverageDTO.setLineCoverage(0.0);
                break;
            default:
                throw new UnsupportedOperationException("Coverage type "
                        + Properties.COVERAGE() + " not yet supported!");
        }

        return coverageDTO;
    }

    /**
     * Returns a string representation of the coverage dto.
     *
     * @return Returns a textual representation of the coverage dto.
     */
    @NonNull
    @Override
    public String toString() {
        return "Coverage{" +
                "activityCoverage=" + activityCoverage +
                ", methodCoverage=" + methodCoverage +
                ", branchCoverage=" + branchCoverage +
                ", lineCoverage=" + lineCoverage +
                '}';
    }
}
