package domain.ui.webstudio.components.editortabcomponents.leftmenu;

import configuration.core.SmartWebElement;
import domain.ui.BasePageComponent;
import lombok.Getter;
import org.openqa.selenium.support.FindBy;

public class TreeFolderComponent extends BasePageComponent {

    //span[contains(@class,'rf-trn-hnd-colps') and following-sibling::span/span/span[text()='%s']]
    @FindBy(xpath = ".//span[contains(@class,'rf-trn-hnd-colps')]")
    private SmartWebElement expanderClosed;

    @Getter
    @FindBy(xpath = ".//span/span/span")
    private SmartWebElement folderName;

    @FindBy(xpath = ".//a[span[text()='%s']]")
    private SmartWebElement item;

    public TreeFolderComponent() {
    }

    public void selectItem(String folderName, String itemName) {
        if(expanderClosed.format(folderName).isDisplayed())
            expanderClosed.format(folderName).click();
        item.format(itemName).click();
    }
}
