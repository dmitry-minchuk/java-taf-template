package domain.ui.webstudio.components.admincomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;

public class PersonalAccessTokenPageComponent extends BaseComponent {

    private WebElement createTokenBtn;
    private WebElement nameInput;
    private WebElement expirationInput;
    private WebElement expirationOptionTemplate;
    private WebElement drawerCreateBtn;
    private WebElement generatedTokenCode;
    private WebElement drawerOkBtn;
    private WebElement tokenRowTemplate;
    private WebElement revokeBtnTemplate;
    private WebElement revokeConfirmOkBtn;

    public PersonalAccessTokenPageComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public PersonalAccessTokenPageComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        createTokenBtn = new WebElement(LocalDriverPool.getPage(),
                "xpath=//button[contains(normalize-space(.),'Create Token')]", "createTokenBtn");
        nameInput = new WebElement(LocalDriverPool.getPage(), "xpath=//input[@id='name']", "tokenNameInput");
        expirationInput = new WebElement(LocalDriverPool.getPage(),
                "xpath=//input[@id='expirationOption']", "tokenExpirationInput");
        expirationOptionTemplate = new WebElement(LocalDriverPool.getPage(),
                "xpath=//div[contains(@class,'ant-select-item-option-content') and normalize-space()='%s']", "expirationOption");
        drawerCreateBtn = new WebElement(LocalDriverPool.getPage(),
                "xpath=//div[contains(@class,'ant-drawer')]//button[.//span[normalize-space()='Create']]", "drawerCreateBtn");
        generatedTokenCode = new WebElement(LocalDriverPool.getPage(),
                "xpath=//code[starts-with(normalize-space(),'openl_pat_')]", "generatedTokenCode");
        drawerOkBtn = new WebElement(LocalDriverPool.getPage(),
                "xpath=//div[contains(@class,'ant-drawer')]//button[.//span[normalize-space()='OK']]", "drawerOkBtn");
        tokenRowTemplate = new WebElement(LocalDriverPool.getPage(),
                "xpath=//tr[contains(@class,'ant-table-row') and .//*[normalize-space()='%s']]", "tokenRow");
        revokeBtnTemplate = new WebElement(LocalDriverPool.getPage(),
                "xpath=//tr[contains(@class,'ant-table-row') and .//*[normalize-space()='%s']]//button[@aria-label='Delete']", "tokenRevokeBtn");
        revokeConfirmOkBtn = new WebElement(LocalDriverPool.getPage(),
                "xpath=//div[contains(@class,'ant-modal')]//button[.//span[normalize-space()='OK']]", "revokeConfirmOkBtn");
    }

    public String createToken(String name, String expirationOption) {
        createTokenBtn.click();
        nameInput.waitForVisible();
        nameInput.fill(name);
        expirationInput.click();
        expirationOptionTemplate.format(expirationOption).click();
        drawerCreateBtn.click();
        generatedTokenCode.waitForVisible();
        String token = generatedTokenCode.getText().trim();
        drawerOkBtn.click();
        drawerOkBtn.waitForHidden(5000);
        return token;
    }

    public boolean isTokenListed(String name) {
        return tokenRowTemplate.format(name).isVisible(2000);
    }

    public void revokeToken(String name) {
        revokeBtnTemplate.format(name).click();
        revokeConfirmOkBtn.click();
        tokenRowTemplate.format(name).waitForHidden(5000);
    }
}
