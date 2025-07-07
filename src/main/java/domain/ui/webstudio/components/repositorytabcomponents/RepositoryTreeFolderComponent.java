package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.BasePageComponent;
import configuration.core.ui.SmartWebElement;
import lombok.Getter;
import org.openqa.selenium.support.FindBy;

public class RepositoryTreeFolderComponent extends BasePageComponent {

    @FindBy(xpath = ".//span[contains(@class,'rf-trn-hnd-colps')]")
    private SmartWebElement expanderClosed;

    @Getter
    @FindBy(xpath = ".//span/span")
    private SmartWebElement folderName;

    @FindBy(xpath = ".//span[text()='%s']")
    private SmartWebElement item;

    public RepositoryTreeFolderComponent() {
    }

    public SmartWebElement getItem(String itemName) {
        return item.format(itemName);
    }

    public void expandFolder() {
        if(expanderClosed.isDisplayed(2))
            expanderClosed.click();
    }

    public void selectItem(String itemName) {
        getItem(itemName).click();
    }
}
