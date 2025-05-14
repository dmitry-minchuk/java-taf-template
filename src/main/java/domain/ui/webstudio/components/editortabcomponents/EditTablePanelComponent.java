package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.BasePageComponent;
import configuration.core.ui.SmartWebElement;
import lombok.Getter;
import org.openqa.selenium.support.FindBy;

@Getter
public class EditTablePanelComponent extends BasePageComponent {

    @FindBy(xpath = "./img[@title='Save changes']")
    private SmartWebElement saveChangesBtn;

    @FindBy(xpath = "./img[@title='Undo changes']")
    private SmartWebElement undoChangesBtn;

    @FindBy(xpath = "./img[@title='Redo changes']")
    private SmartWebElement redoChangesBtn;

    @FindBy(xpath = "./img[@title='Insert row after']")
    private SmartWebElement insertRowAfterBtn;

    @FindBy(xpath = "./img[@title='Remove row']")
    private SmartWebElement removeRowBtn;

    @FindBy(xpath = "./img[@title='Insert column before']")
    private SmartWebElement insertColumnBeforeBtn;

    @FindBy(xpath = "./img[@title='Remove column']")
    private SmartWebElement removeColumnBtn;

    @FindBy(xpath = "./img[@title='Align the text to the left']")
    private SmartWebElement alignTextLeftBtn;

    @FindBy(xpath = "./img[@title='Center the text']")
    private SmartWebElement centerTextBtn;

    @FindBy(xpath = "./img[@title='Align the text to the right']")
    private SmartWebElement alignTextRightBtn;

    @FindBy(xpath = "./img[@title='Make the text bold']")
    private SmartWebElement makeTextBoldBtn;

    @FindBy(xpath = "./img[@title='Italicize the text']")
    private SmartWebElement italicizeTextBtn;

    @FindBy(xpath = "./img[@title='Underline the text']")
    private SmartWebElement underlineTextBtn;

    @FindBy(xpath = "./img[@title='Color the cell background']")
    private SmartWebElement colorCellBackgroundBtn;

    @FindBy(xpath = "./img[@title='Color the cell text']")
    private SmartWebElement colorCellTextBtn;

    @FindBy(xpath = "./img[@title='Decrease indent']")
    private SmartWebElement decreaseIndentBtn;

    @FindBy(xpath = "./img[@title='Increase indent']")
    private SmartWebElement increaseIndentBtn;

    public EditTablePanelComponent() {
    }

}
