package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.BasePageComponent;
import configuration.core.ui.SmartWebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class ProblemsPanelComponent extends BasePageComponent {

    @FindBy(xpath = ".//div[@class='ui-layout-toggler ui-layout-toggler-south ui-layout-toggler-closed ui-layout-toggler-south-closed' and @title='Open']")
    private SmartWebElement showProblemsLink;

    @FindBy(xpath = ".//div[@id='bottom']//span[@id='south-closer']")
    private SmartWebElement hideProblemPanelLink;

    @FindBy(id = "errors-count")
    private SmartWebElement errorsCounter;

    @FindBy(id = "warnings-count")
    private SmartWebElement warningsCounter;

    @FindBy(xpath = ".//div[@id='errors-panel']//a")
    private List<SmartWebElement> errorsAll;

    @FindBy(xpath = ".//div[@id='warnings-panel']//a")
    private List<SmartWebElement> warningsAll;

    @FindBy(xpath = ".//div[@class='panel']//div[@id='progress-info-panel']")
    private SmartWebElement compilationProgressBar;

    @FindBy(xpath = ".//div[contains(@class,'ui-layout-resizer')]//div[@class='messagePanel']")
    private SmartWebElement compilationProgressBarNotSavedProject;

    public ProblemsPanelComponent() {
    }

    public void showProblemsPanel() {
        if(showProblemsLink.isDisplayed())
            showProblemsLink.click();
    }

    public void hideProblemsPanel() {
        if(hideProblemPanelLink.isDisplayed())
            hideProblemPanelLink.click();
    }

    public Integer getNumberOfErrorsIfPresent() {
        if(errorsCounter.isDisplayed())
            return Integer.parseInt(errorsCounter.getText());
        else
            return null;
    }

    public Integer getNumberOfWarningsIfPresent() {
        if(warningsCounter.isDisplayed())
            return Integer.parseInt(warningsCounter.getText());
        else
            return null;
    }

    public List<String> getAllErrors() {
        return errorsAll.stream().map(SmartWebElement::getText).toList();
    }

    public List<String> getAllWarnings() {
        return warningsAll.stream().map(SmartWebElement::getText).toList();
    }

    public void selectProblemByIndex(int index) {
        if (index > 0 && index <= errorsAll.size()) {
            errorsAll.get(index - 1).click();
        }
    }

    public void selectProblemByText(String text) {
        errorsAll.stream()
                .filter(error -> error.getText().contains(text))
                .findFirst()
                .ifPresent(SmartWebElement::click);
    }

}
