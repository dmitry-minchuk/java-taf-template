package domain.ui.webstudio.components.editortabcomponents.toolbar;

import java.util.List;

/** The Run dropdown of the table toolbar: input parameters and the launch button. */
public interface IRunMenu {
    IRunMenu clickCreateItem();
    IRunMenu clickAddElementToCollectionBtn(String containsText);
    IRunMenu clickExpandCollection();
    IRunMenu clickRunInsideMenu();
    IRunMenu clickAddedElementsExpander(String containsText);
    List<String> getAliasDropdownValues();
    IRunMenu setInputTextField(String index, String value);
    IRunMenu setInputSelectField(String index, String value);
}
