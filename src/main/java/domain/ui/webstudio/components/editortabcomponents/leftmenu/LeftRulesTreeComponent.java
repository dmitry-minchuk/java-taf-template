package domain.ui.webstudio.components.editortabcomponents.leftmenu;

import configuration.core.ui.SmartWebElement;
import configuration.core.ui.BasePageComponent;
import lombok.Getter;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class LeftRulesTreeComponent extends BasePageComponent {

    @FindAll({
            @FindBy(xpath = ".//div[@id='rulesTree']//div[./div/span[contains(@class,'rf-trn-hnd-colps')] and contains(@class, 'rf-tr-nd-colps')]"),
            @FindBy(xpath = ".//div[@id='rulesTree']//div[./div/span[contains(@class,'rf-trn-hnd-colps')] and contains(@class, 'rf-tr-nd-exp')]"),
            @FindBy(xpath = ".//div[@id='rulesTree']//div[./div/span[contains(@class,'rf-trn-hnd-exp')] and contains(@class, 'rf-tr-nd-exp')]")
    }) //Looking for all folders (even nested) - so actions on nested ones will depend on ancestor-folders
    private List<TreeFolderComponent> treeFolderComponentList;

    @FindBy(xpath = ".//div[@id='rulesTree']/div")
    private List<SmartWebElement> testList;

    @FindBy(xpath = ".//div[@class='filter-view']/span/a")
    private SmartWebElement viewFilterLink;

    @FindBy(xpath = ".//ul[@class='dropdown-menu link-dropdown-menu']/li/a[text()='%s']")
    private SmartWebElement viewFilterOptionsLink;

    public LeftRulesTreeComponent() {
    }

    public void setViewFilter(FilterOptions filterOption) {
        viewFilterLink.click();
        viewFilterOptionsLink.format(filterOption.getValue()).click();
    }

    public void selectItemInTree(String folderName, String itemName) {
        TreeFolderComponent folder = treeFolderComponentList.stream().
                filter(c -> c.getFolderName().getText().equals(folderName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Folder with name %s not found", folderName)));
        folder.selectItem(folderName, itemName);
    }

    public boolean isItemExistsInTree(String itemName) {
        return treeFolderComponentList.stream().anyMatch(c -> c.getItem(itemName).isDisplayed(1));
    }

    public void expandFolderInTree(String folderName) {
        TreeFolderComponent folder = treeFolderComponentList.stream().
                filter(c -> c.getFolderName().getText().equals(folderName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Folder with name %s not found", folderName)));
        folder.expandFolder(folderName);
    }

    public boolean isFolderExistsInTree(String folderName) {
        return treeFolderComponentList.stream().
                anyMatch(c -> c.getFolderName().getText().equals(folderName));
    }

    @Getter
    public enum FilterOptions {
        BY_TYPE("By Type"),
        BY_EXCEL_SHEET("By Excel Sheet"),
        BY_CATEGORY("By Category"),
        BY_CATEGORY_DETAILED("By Category Detailed"),
        BY_CATEGORY_INVERSED("By Category Inversed");

        private String value;

        FilterOptions(String value) {
            this.value = value;
        }
    }

}
