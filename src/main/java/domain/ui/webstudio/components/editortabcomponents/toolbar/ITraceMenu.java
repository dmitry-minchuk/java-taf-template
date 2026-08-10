package domain.ui.webstudio.components.editortabcomponents.toolbar;

import java.util.List;

/** The Trace dropdown of the table toolbar. */
public interface ITraceMenu {
    ITraceMenu setFactorTextField(String text);
    ITraceMenu selectJSONTrace(String json);
    ITraceMenu clickTraceIntoFile();
    ITraceWindow clickTraceInsideMenu();
    ITraceWindow clickTraceInsideMenu(boolean isPopupExpected);
    List<String> getAliasDropdownValues();
}
