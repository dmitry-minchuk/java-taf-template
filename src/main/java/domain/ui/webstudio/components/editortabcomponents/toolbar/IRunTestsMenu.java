package domain.ui.webstudio.components.editortabcomponents.toolbar;

/** The Test dropdown of the top toolbar: run settings and the Test launch button. */
public interface IRunTestsMenu {
    IRunTestsMenu setTestPerPage(String testsPerPage);
    IRunTestsMenu setFailuresOnly(boolean failuresOnly);
    IRunTestsMenu setCompoundResult(boolean compoundResult);
    void runTests();
    String getTestPerPage();
    boolean isFailuresOnlyChecked();
    boolean isCompoundResultChecked();
}
