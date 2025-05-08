package domain.ui.webstudio.components.editortabcomponents.leftmenu;

import configuration.core.ui.SmartWebElement;
import configuration.core.ui.BasePageComponent;
import lombok.Getter;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class LeftRulesTreeComponent extends BasePageComponent {

    @FindBy(xpath = ".//div[@id='rulesTree']/div")
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
        testList.size();
        treeFolderComponentList.size();
        TreeFolderComponent folder = treeFolderComponentList.stream().
                filter(c -> c.getFolderName().getText().equals(folderName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Folder with name %s not found", folderName)));
        folder.selectItem(folderName, itemName);
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
