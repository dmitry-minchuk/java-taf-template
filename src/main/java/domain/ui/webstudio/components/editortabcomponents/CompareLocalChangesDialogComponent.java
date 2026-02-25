package domain.ui.webstudio.components.editortabcomponents;

import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

import java.util.List;
import java.util.stream.Collectors;

public class CompareLocalChangesDialogComponent extends BaseComponent {

    private WebElement treeContainer;
    private WebElement closeBtn;
    private List<WebElement> treeItems;
    private Page comparePopup;

    public CompareLocalChangesDialogComponent(Page comparePopup) {
        super(comparePopup);
        this.comparePopup = comparePopup;
        initializeElements();
    }

    private void initializeElements() {
        treeContainer = new WebElement(getPage(), "xpath=//div[@id='diffTreeForm:newTree']", "treeContainer");
        closeBtn = new WebElement(getPage(), "xpath=//input[@value='Close']", "closeBtn");
        treeItems = createElementList("xpath=//div[@id='diffTreeForm:newTree']//span[@class='rf-trn-lbl']", "treeItems");
    }

    public CompareLocalChangesDialogComponent waitForDialogToAppear() {
        WaitUtil.sleep(1500, "Waiting for Local Changes Compare dialog to appear");
        treeContainer.waitForVisible(5000);
        return this;
    }

    public List<String> getLeftModulesList() {
        return treeItems.stream().map(WebElement::getText).collect(Collectors.toList());
    }

    public void close() {
        if (comparePopup != null && !comparePopup.isClosed()) {
            comparePopup.close();
        }
    }
}
