package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.TestCaseId;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestWhichIsGoingToFail extends BaseTest {

    @Test
    @TestCaseId("EPBDS-34242")
    public void testWhichIsGoingToFail() {
        assertThat(4).isGreaterThan(5).as("Some description here");
    }
}
