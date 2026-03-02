package domain.ui.webstudio.components.editortabcomponents;

import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import helpers.utils.WaitUtil;

import java.nio.file.Paths;

public class CompareExcelFilesDialogComponent extends CompareLocalChangesDialogComponent {

    // File upload area: hidden input inside the RichFaces file upload widget
    private WebElement fileUploadInput;
    // "Add" button span element (visible when fewer than 2 files uploaded)
    private WebElement addButtonElement;
    // Second item in the upload list (for CSS border check)
    private WebElement secondElementInUpload;
    // "Clear All" button
    private WebElement clearAllBtn;
    // "Compare" button for Excel comparison
    private WebElement compareExcelBtn;
    // "Show equal rows" checkbox in the diffTreeForm
    private WebElement showEqualRowsCheckboxExcel;
    // "Show equal elements" checkbox (shows equal nodes in tree)
    private WebElement showEqualElementsCheckbox;

    public CompareExcelFilesDialogComponent(Page comparePopup) {
        super(comparePopup);
        initializeExcelElements();
    }

    private void initializeExcelElements() {
        fileUploadInput = new WebElement(getPage(),
                "xpath=//div[@id='diffForm:fileUpload']//input[@type='file']",
                "fileUploadInput");
        addButtonElement = new WebElement(getPage(),
                "xpath=//div[@id='diffForm:fileUpload']//span[@class='rf-fu-btn-cnt-add']",
                "addButtonElement");
        secondElementInUpload = new WebElement(getPage(),
                "xpath=(//div[@id='diffForm:fileUpload']//div[@class='rf-fu-itm'])[2]",
                "secondElementInUpload");
        clearAllBtn = new WebElement(getPage(),
                "xpath=//span[@class='rf-fu-btn-cnt-clr']",
                "clearAllBtn");
        compareExcelBtn = new WebElement(getPage(),
                "xpath=//input[@id='diffForm:compareButton']",
                "compareExcelBtn");
        showEqualRowsCheckboxExcel = new WebElement(getPage(),
                "xpath=//form[@id='diffTreeForm']//input[@type='checkbox']",
                "showEqualRowsCheckboxExcel");
        showEqualElementsCheckbox = new WebElement(getPage(),
                "xpath=//div[@id='diffFormPanel']//input[@type='checkbox']",
                "showEqualElementsCheckbox");
    }

    public CompareExcelFilesDialogComponent uploadFile(String absoluteFilePath) {
        fileUploadInput.getLocator().setInputFiles(Paths.get(absoluteFilePath));
        WaitUtil.sleep(1000, "Waiting for file to upload: " + absoluteFilePath);
        return this;
    }

    public boolean isAddButtonPresent() {
        return addButtonElement.isVisible(2000);
    }

    public String getSecondElementBorderBottomStyle() {
        return secondElementInUpload.getCssValue("border-bottom-style");
    }

    public CompareExcelFilesDialogComponent clickClearAll() {
        clearAllBtn.click();
        WaitUtil.sleep(500, "Waiting after Clear All click");
        return this;
    }

    public boolean isCompareExcelBtnPresent() {
        return compareExcelBtn.isVisible(2000);
    }

    public boolean isCompareExcelBtnEnabled() {
        return compareExcelBtn.isEnabled();
    }

    public CompareExcelFilesDialogComponent setShowEqualRowsExcel(boolean value) {
        if (showEqualRowsCheckboxExcel.isChecked() != value) {
            showEqualRowsCheckboxExcel.click();
            WaitUtil.sleep(300, "Waiting for showEqualRows toggle");
        }
        return this;
    }

    public CompareExcelFilesDialogComponent setShowEqualElements(boolean value) {
        if (showEqualElementsCheckbox.isChecked() != value) {
            showEqualElementsCheckbox.click();
            WaitUtil.sleep(300, "Waiting for showEqualElements toggle");
        }
        return this;
    }

    public void clickCompareExcel() {
        compareExcelBtn.click();
        WaitUtil.sleep(2000, "Waiting for Excel comparison to complete");
    }
}
