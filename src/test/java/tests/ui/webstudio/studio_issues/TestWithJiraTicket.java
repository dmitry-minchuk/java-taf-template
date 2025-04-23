package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.TestCaseId;
import org.testng.annotations.Test;
import tests.BaseTest;

public class TestWithJiraTicket extends BaseTest {

    @Test
    @TestCaseId("EPBDS-12345")
    public void testWithJiraTicket() {
        System.out.println("This test is linked to a Jira ticket.");
    }
}
