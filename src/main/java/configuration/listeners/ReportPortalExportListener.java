package configuration.listeners;

import helpers.utils.ReportPortalArtifactUtil;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ReportPortalExportListener implements ITestListener {

    @Override
    public void onTestSuccess(ITestResult result) {
        ReportPortalArtifactUtil.recordTestResultIfMissing(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ReportPortalArtifactUtil.recordTestResultIfMissing(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ReportPortalArtifactUtil.recordTestResultIfMissing(result);
    }
}
