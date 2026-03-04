package domain.ui.webstudio.components.editortabcomponents;

import com.microsoft.playwright.Dialog;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

import java.util.List;

public class EditorRevisionsTabComponent extends BaseComponent {

    // Revisions table shown in the editor after clicking More → Revisions
    private List<WebElement> revisionRows;
    // Action link template (first link in each row's last column)
    private WebElement actionLinkTemplate;

    public EditorRevisionsTabComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public EditorRevisionsTabComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        revisionRows = createElementList(
                "xpath=//div[@id='content']//table//tr[position()>1]",
                "revisionRows");
        actionLinkTemplate = new WebElement(page,
                "xpath=(//div[@id='content']//table//tr)[%s]//td[last()]//a",
                "actionLinkTemplate");
    }

    public void waitForTableToLoad() {
        WaitUtil.waitForCondition(() -> !revisionRows.isEmpty(), 5000, 250,
                "Waiting for revisions table to load");
    }

    public String getCommentForRow(int rowIndex) {
        WebElement commentCell = new WebElement(page,
                String.format("xpath=(//div[@id='content']//table//tr)[%s]//td[contains(@class,'comment') or position()=last()-1]", rowIndex + 1),
                "commentCell_" + rowIndex);
        return commentCell.getText().trim();
    }

    public void openRevision(int rowIndex) {
        // rowIndex is 1-based (1 = most recent, 2 = one before, etc.)
        // The table header is row 1, so data rows start at position 2
        int tableRowPosition = rowIndex + 1;
        WebElement actionLink = actionLinkTemplate.format(String.valueOf(tableRowPosition));
        // Accept the browser alert that confirms opening the old revision
        page.onDialog(Dialog::accept);
        actionLink.click();
        WaitUtil.sleep(2000, "Waiting after opening revision " + rowIndex);
    }

    public int getRowCount() {
        waitForTableToLoad();
        return revisionRows.size();
    }
}
