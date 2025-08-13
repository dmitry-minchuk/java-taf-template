package domain.ui.webstudio.components;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightTestRunDropDownComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement testPerPageField;
    private PlaywrightWebElement failuresOnlyCheckbox;
    private PlaywrightWebElement failuresPerTestField;
    private PlaywrightWebElement compoundResultCheckbox;

    public PlaywrightTestRunDropDownComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightTestRunDropDownComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        testPerPageField = createScopedElement("xpath=.//select[contains(@id,'testPerPage')] | .//input[contains(@id,'testPerPage')]", "testPerPageField");
        failuresOnlyCheckbox = createScopedElement("xpath=.//input[@type='checkbox' and contains(@id,'failuresOnly')]", "failuresOnlyCheckbox");
        failuresPerTestField = createScopedElement("xpath=.//select[contains(@id,'failuresPerTest')] | .//input[contains(@id,'failuresPerTest')]", "failuresPerTestField");
        compoundResultCheckbox = createScopedElement("xpath=.//input[@type='checkbox' and contains(@id,'compoundResult')]", "compoundResultCheckbox");
    }

    public String getTestPerPage() {
        return testPerPageField.getAttribute("value");
    }

    public boolean isFailuresOnlyEnabled() {
        return failuresOnlyCheckbox.isSelected();
    }

    public String getFailuresPerTest() {
        return failuresPerTestField.getAttribute("value");
    }

    public boolean isCompoundResultEnabled() {
        return compoundResultCheckbox.isSelected();
    }

    public void setTestPerPage(String value) {
        // Try as select first, if fails treat as input
        try {
            testPerPageField.selectByVisibleText(value);
        } catch (Exception e) {
            testPerPageField.fill(value);
        }
    }

    public void setFailuresOnly(boolean enabled) {
        if (enabled != failuresOnlyCheckbox.isSelected()) {
            failuresOnlyCheckbox.click();
        }
    }

    public void setFailuresPerTest(String value) {
        // Try as select first, if fails treat as input
        try {
            failuresPerTestField.selectByVisibleText(value);
        } catch (Exception e) {
            failuresPerTestField.fill(value);
        }
    }

    public void setCompoundResult(boolean enabled) {
        if (enabled != compoundResultCheckbox.isSelected()) {
            compoundResultCheckbox.click();
        }
    }

    public boolean validateTestRunSettings(String expectedTestsPerPage, Boolean expectedFailuresOnly, 
                                         String expectedFailuresPerTest, Boolean expectedCompoundResult) {
        boolean testsPerPageMatches = expectedTestsPerPage == null || expectedTestsPerPage.equals(getTestPerPage());
        boolean failuresOnlyMatches = expectedFailuresOnly == null || expectedFailuresOnly.equals(isFailuresOnlyEnabled());
        boolean failuresPerTestMatches = expectedFailuresPerTest == null || expectedFailuresPerTest.equals(getFailuresPerTest());
        boolean compoundResultMatches = expectedCompoundResult == null || expectedCompoundResult.equals(isCompoundResultEnabled());
        
        return testsPerPageMatches && failuresOnlyMatches && failuresPerTestMatches && compoundResultMatches;
    }

    public String getTestRunDropDownInfo() {
        return String.format("TestRunDropDown - TestsPerPage: %s | FailuresOnly: %s | FailuresPerTest: %s | CompoundResult: %s",
                getTestPerPage(),
                isFailuresOnlyEnabled(),
                getFailuresPerTest(),
                isCompoundResultEnabled());
    }
}