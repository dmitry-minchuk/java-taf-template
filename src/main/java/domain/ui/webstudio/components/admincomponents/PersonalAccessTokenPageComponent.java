package domain.ui.webstudio.components.admincomponents;

import configuration.core.ui.WebElement;
import configuration.driver.DriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

public class PersonalAccessTokenPageComponent extends BaseComponent {

    private static final int PROBE_MS = DEFAULT_TIMEOUT_MS / 5;
    private static final int TOOLTIP_PROBE_MS = 1000;

    private WebElement createTokenBtn;
    private WebElement nameInput;
    private WebElement expirationInput;
    private WebElement expirationOptionTemplate;
    private WebElement drawerCreateBtn;
    private WebElement generatedTokenCode;
    private WebElement drawerOkBtn;
    private WebElement drawerCancelBtn;
    private WebElement copyGeneratedTokenBtn;
    private WebElement copyConfirmationTooltip;
    private WebElement copyFailureNotification;
    private WebElement tokenRowTemplate;
    private WebElement revokeBtnTemplate;
    private WebElement revokeConfirmOkBtn;

    public PersonalAccessTokenPageComponent() {
        super(DriverPool.getPage());
        initializeElements();
    }

    public PersonalAccessTokenPageComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        createTokenBtn = new WebElement(DriverPool.getPage(),
                "xpath=//button[contains(normalize-space(.),'Create Token')]", "createTokenBtn");
        nameInput = new WebElement(DriverPool.getPage(), "xpath=//input[@id='name']", "tokenNameInput");
        expirationInput = new WebElement(DriverPool.getPage(),
                "xpath=//input[@id='expirationOption']", "tokenExpirationInput");
        expirationOptionTemplate = new WebElement(DriverPool.getPage(),
                "xpath=//div[contains(@class,'ant-select-item-option-content') and normalize-space()='%s']", "expirationOption");
        drawerCreateBtn = new WebElement(DriverPool.getPage(),
                "xpath=//div[contains(@class,'ant-drawer')]//button[.//span[normalize-space()='Create']]", "drawerCreateBtn");
        generatedTokenCode = new WebElement(DriverPool.getPage(),
                "xpath=//code[starts-with(normalize-space(),'openl_pat_')]", "generatedTokenCode");
        drawerOkBtn = new WebElement(DriverPool.getPage(),
                "xpath=//div[contains(@class,'ant-drawer')]//button[.//span[normalize-space()='OK']]", "drawerOkBtn");
        drawerCancelBtn = new WebElement(DriverPool.getPage(),
                "xpath=//div[contains(@class,'ant-drawer')]//button[.//span[normalize-space()='Cancel']]", "drawerCancelBtn");
        copyGeneratedTokenBtn = new WebElement(DriverPool.getPage(),
                "xpath=//div[contains(@class,'ant-drawer')]//button[.//*[name()='svg' and @data-icon='copy']]", "copyGeneratedTokenBtn");
        copyConfirmationTooltip = new WebElement(DriverPool.getPage(),
                "xpath=//div[contains(concat(' ', normalize-space(@class), ' '), ' ant-tooltip-container ')]"
                        + "[contains(normalize-space(.),'Token copied to clipboard')]", "copyConfirmationTooltip");
        copyFailureNotification = new WebElement(DriverPool.getPage(),
                "xpath=//div[contains(concat(' ', normalize-space(@class), ' '), ' ant-notification-notice-title ')]"
                        + "[contains(normalize-space(.),'Failed to copy to clipboard')]", "copyFailureNotification");
        tokenRowTemplate = new WebElement(DriverPool.getPage(),
                "xpath=//tr[contains(@class,'ant-table-row') and .//*[normalize-space()='%s']]", "tokenRow");
        revokeBtnTemplate = new WebElement(DriverPool.getPage(),
                "xpath=//tr[contains(@class,'ant-table-row') and .//*[normalize-space()='%s']]//button[@aria-label='Delete']", "tokenRevokeBtn");
        revokeConfirmOkBtn = new WebElement(DriverPool.getPage(),
                "xpath=//div[contains(@class,'ant-modal')]//button[.//span[normalize-space()='OK']]", "revokeConfirmOkBtn");
    }

    public String createToken(String name, String expirationOption) {
        String token = createTokenKeepingResultOpen(name, expirationOption);
        confirmGeneratedToken();
        return token;
    }

    public String createTokenKeepingResultOpen(String name, String expirationOption) {
        createTokenBtn.click();
        nameInput.waitForVisible();
        nameInput.fill(name);
        expirationInput.click();
        expirationOptionTemplate.format(expirationOption).click();
        drawerCreateBtn.click();
        generatedTokenCode.waitForVisible();
        return generatedTokenCode.getText().trim();
    }

    public void confirmGeneratedToken() {
        drawerOkBtn.click();
        drawerOkBtn.waitForHidden(DEFAULT_TIMEOUT_MS);
    }

    public void copyGeneratedToken() {
        copyGeneratedTokenBtn.click();
    }

    public boolean isCopyConfirmationDisplayed() {
        return copyConfirmationTooltip.isVisible(TOOLTIP_PROBE_MS);
    }

    public boolean isCopyFailureDisplayed() {
        return copyFailureNotification.isVisible(PROBE_MS);
    }

    public String pasteIntoNewTokenName() {
        createTokenBtn.click();
        nameInput.waitForVisible();
        nameInput.click();
        nameInput.press("ControlOrMeta+v");
        WaitUtil.waitForCondition(() -> !nameInput.getCurrentInputValue().isBlank(),
                DEFAULT_TIMEOUT_MS, 200, "Waiting for the pasted token to land in the name field");
        String pasted = nameInput.getCurrentInputValue();
        drawerCancelBtn.click();
        drawerCancelBtn.waitForHidden(DEFAULT_TIMEOUT_MS);
        return pasted;
    }

    public boolean isTokenListed(String name) {
        return tokenRowTemplate.format(name).isVisible(PROBE_MS);
    }

    public void revokeToken(String name) {
        revokeBtnTemplate.format(name).click();
        revokeConfirmOkBtn.click();
        WebElement row = tokenRowTemplate.format(name);
        if (row.isVisible(DEFAULT_TIMEOUT_MS)) {
            getPage().reload();
            waitUntilSpinnerLoaded();
            row.waitForHidden(DEFAULT_TIMEOUT_MS);
        }
    }
}
