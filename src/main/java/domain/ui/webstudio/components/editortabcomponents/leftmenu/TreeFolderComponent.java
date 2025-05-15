package domain.ui.webstudio.components.editortabcomponents.leftmenu;

import configuration.core.ui.SmartWebElement;
import configuration.core.ui.BasePageComponent;
import helpers.utils.PrintUtil;
import lombok.Getter;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class TreeFolderComponent extends BasePageComponent {

    @FindBy(xpath = ".//span[contains(@class,'rf-trn-hnd-colps')]")
    private SmartWebElement expanderClosed;

    @Getter
    @FindBy(xpath = ".//span/span/span")
    private SmartWebElement folderName;

    @FindBy(xpath = ".//a[span[text()='%s']]")
    private SmartWebElement item;

    public TreeFolderComponent() {
    }

    public SmartWebElement getItem(String itemName) {
        return item.format(itemName);
    }

    public void expandFolder(String folderName) {
        if(expanderClosed.format(folderName).isDisplayed())
            expanderClosed.format(folderName).click();
    }

    public void selectItem(String folderName, String itemName) {
        expandFolder(folderName);
        getItem(itemName).click();
    }
}
