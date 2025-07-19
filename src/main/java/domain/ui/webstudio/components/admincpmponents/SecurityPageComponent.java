package domain.ui.webstudio.components.admincpmponents;

import configuration.core.ui.BasePageComponent;
import configuration.core.ui.SmartWebElement;
import helpers.utils.WaitUtil;
import org.openqa.selenium.support.FindBy;


public class SecurityPageComponent extends BasePageComponent {

    // Authentication Mode Section
    @FindBy(xpath = ".//input[@type='radio' and @value='single']")
    private SmartWebElement singleUserModeRadio;

    @FindBy(xpath = ".//input[@type='radio' and @value='multi']")
    private SmartWebElement multiUserModeRadio;

    @FindBy(xpath = ".//input[@type='radio' and @value='ad']")
    private SmartWebElement activeDirectoryModeRadio;

    @FindBy(xpath = ".//input[@type='radio' and @value='saml']")
    private SmartWebElement samlModeRadio;

    @FindBy(xpath = ".//input[@type='radio' and @value='oauth2']")
    private SmartWebElement oauth2ModeRadio;

    // Administrator Configuration Section
    @FindBy(xpath = ".//input[@placeholder='Administrator Username']")
    private SmartWebElement adminUsernameField;

    @FindBy(xpath = ".//input[@placeholder='Administrator Password']")
    private SmartWebElement adminPasswordField;

    @FindBy(xpath = ".//input[@placeholder='Confirm Administrator Password']")
    private SmartWebElement adminConfirmPasswordField;

    @FindBy(xpath = ".//input[@placeholder='Administrator Full Name']")
    private SmartWebElement adminFullNameField;

    @FindBy(xpath = ".//input[@placeholder='Administrator Email']")
    private SmartWebElement adminEmailField;

    // Default Group Settings
    @FindBy(xpath = ".//input[@placeholder='Default Group']")
    private SmartWebElement defaultGroupField;

    @FindBy(xpath = ".//input[@type='checkbox' and @id='createDefaultGroup']")
    private SmartWebElement createDefaultGroupCheckbox;

    // Active Directory Configuration
    @FindBy(xpath = ".//input[@placeholder='AD Server URL']")
    private SmartWebElement adServerUrlField;

    @FindBy(xpath = ".//input[@placeholder='AD Domain']")
    private SmartWebElement adDomainField;

    @FindBy(xpath = ".//input[@placeholder='AD Search Base']")
    private SmartWebElement adSearchBaseField;

    @FindBy(xpath = ".//input[@placeholder='AD Search Filter']")
    private SmartWebElement adSearchFilterField;

    @FindBy(xpath = ".//input[@placeholder='AD Username Attribute']")
    private SmartWebElement adUsernameAttributeField;

    @FindBy(xpath = ".//input[@placeholder='AD Email Attribute']")
    private SmartWebElement adEmailAttributeField;

    // SAML Configuration
    @FindBy(xpath = ".//input[@placeholder='SAML SSO URL']")
    private SmartWebElement samlSsoUrlField;

    @FindBy(xpath = ".//input[@placeholder='SAML Entity ID']")
    private SmartWebElement samlEntityIdField;

    @FindBy(xpath = ".//input[@placeholder='SAML Certificate']")
    private SmartWebElement samlCertificateField;

    @FindBy(xpath = ".//input[@placeholder='SAML Username Attribute']")
    private SmartWebElement samlUsernameAttributeField;

    @FindBy(xpath = ".//input[@placeholder='SAML Email Attribute']")
    private SmartWebElement samlEmailAttributeField;

    // OAuth2 Configuration
    @FindBy(xpath = ".//input[@placeholder='OAuth2 Client ID']")
    private SmartWebElement oauth2ClientIdField;

    @FindBy(xpath = ".//input[@placeholder='OAuth2 Client Secret']")
    private SmartWebElement oauth2ClientSecretField;

    @FindBy(xpath = ".//input[@placeholder='OAuth2 Authorization URL']")
    private SmartWebElement oauth2AuthUrlField;

    @FindBy(xpath = ".//input[@placeholder='OAuth2 Token URL']")
    private SmartWebElement oauth2TokenUrlField;

    @FindBy(xpath = ".//input[@placeholder='OAuth2 User Info URL']")
    private SmartWebElement oauth2UserInfoUrlField;

    @FindBy(xpath = ".//input[@placeholder='OAuth2 Scope']")
    private SmartWebElement oauth2ScopeField;

    // Action Buttons
    @FindBy(xpath = ".//button[./span[text()='Apply']]")
    private SmartWebElement applyBtn;

    @FindBy(xpath = ".//button[./span[text()='Save']]")
    private SmartWebElement saveBtn;

    @FindBy(xpath = ".//button[./span[text()='Test Connection']]")
    private SmartWebElement testConnectionBtn;

    @FindBy(xpath = ".//button[./span[text()='Reset']]")
    private SmartWebElement resetBtn;


    // Authentication Mode Methods
    
    public void selectSingleUserMode() {
        singleUserModeRadio.click();
    }

    
    public void selectMultiUserMode() {
        multiUserModeRadio.click();
    }

    
    public void selectActiveDirectoryMode() {
        activeDirectoryModeRadio.click();
    }

    
    public void selectSamlMode() {
        samlModeRadio.click();
    }

    
    public void selectOAuth2Mode() {
        oauth2ModeRadio.click();
    }

    
    public String getSelectedAuthMode() {
        if (singleUserModeRadio.isSelected()) return "single";
        if (multiUserModeRadio.isSelected()) return "multi";
        if (activeDirectoryModeRadio.isSelected()) return "ad";
        if (samlModeRadio.isSelected()) return "saml";
        if (oauth2ModeRadio.isSelected()) return "oauth2";
        return "unknown";
    }

    // Administrator Configuration Methods
    
    public void setAdminUsername(String username) {
        adminUsernameField.sendKeys(username);
    }

    
    public void setAdminPassword(String password) {
        adminPasswordField.sendKeys(password);
    }

    
    public void setAdminConfirmPassword(String confirmPassword) {
        adminConfirmPasswordField.sendKeys(confirmPassword);
    }

    
    public void setAdminFullName(String fullName) {
        adminFullNameField.sendKeys(fullName);
    }

    
    public void setAdminEmail(String email) {
        adminEmailField.sendKeys(email);
    }

    
    public void configureAdministrator(String username, String password, String fullName, String email) {
        setAdminUsername(username);
        setAdminPassword(password);
        setAdminConfirmPassword(password);
        setAdminFullName(fullName);
        setAdminEmail(email);
    }

    // Default Group Methods
    
    public void setDefaultGroup(String groupName) {
        defaultGroupField.sendKeys(groupName);
    }

    
    public void setCreateDefaultGroup(boolean create) {
        if (create != createDefaultGroupCheckbox.isSelected()) {
            createDefaultGroupCheckbox.click();
        }
    }

    
    public String getDefaultGroup() {
        return defaultGroupField.getAttribute("value");
    }

    
    public boolean isCreateDefaultGroupEnabled() {
        return createDefaultGroupCheckbox.isSelected();
    }

    // Active Directory Configuration Methods
    
    public void configureActiveDirectory(String serverUrl, String domain, String searchBase, 
                                       String searchFilter, String usernameAttribute, String emailAttribute) {
        adServerUrlField.sendKeys(serverUrl);
        adDomainField.sendKeys(domain);
        adSearchBaseField.sendKeys(searchBase);
        adSearchFilterField.sendKeys(searchFilter);
        adUsernameAttributeField.sendKeys(usernameAttribute);
        adEmailAttributeField.sendKeys(emailAttribute);
    }

    // SAML Configuration Methods
    
    public void configureSaml(String ssoUrl, String entityId, String certificate, 
                             String usernameAttribute, String emailAttribute) {
        samlSsoUrlField.sendKeys(ssoUrl);
        samlEntityIdField.sendKeys(entityId);
        samlCertificateField.sendKeys(certificate);
        samlUsernameAttributeField.sendKeys(usernameAttribute);
        samlEmailAttributeField.sendKeys(emailAttribute);
    }

    // OAuth2 Configuration Methods
    
    public void configureOAuth2(String clientId, String clientSecret, String authUrl, 
                               String tokenUrl, String userInfoUrl, String scope) {
        oauth2ClientIdField.sendKeys(clientId);
        oauth2ClientSecretField.sendKeys(clientSecret);
        oauth2AuthUrlField.sendKeys(authUrl);
        oauth2TokenUrlField.sendKeys(tokenUrl);
        oauth2UserInfoUrlField.sendKeys(userInfoUrl);
        oauth2ScopeField.sendKeys(scope);
    }

    // Action Methods
    
    public void applySettings() {
        applyBtn.click();
    }

    
    public void saveSettings() {
        saveBtn.click();
    }

    
    public void testConnection() {
        testConnectionBtn.click();
    }

    
    public void resetSettings() {
        resetBtn.click();
    }

    
    public void confirmAction() {
        getConfirmationPopup().confirm();
    }

    
    public void cancelAction() {
        getConfirmationPopup().cancel();
    }

    // Complex Configuration Methods
    
    public void configureMultiUserMode(String adminUsername, String adminPassword, String adminFullName, 
                                      String adminEmail, String defaultGroup, boolean createDefaultGroup) {
        selectMultiUserMode();
        configureAdministrator(adminUsername, adminPassword, adminFullName, adminEmail);
        setDefaultGroup(defaultGroup);
        setCreateDefaultGroup(createDefaultGroup);
    }

    
    public void configureActiveDirectoryMode(String serverUrl, String domain, String searchBase, 
                                           String searchFilter, String usernameAttribute, String emailAttribute, 
                                           String defaultGroup) {
        selectActiveDirectoryMode();
        configureActiveDirectory(serverUrl, domain, searchBase, searchFilter, usernameAttribute, emailAttribute);
        setDefaultGroup(defaultGroup);
    }

    
    public void configureSamlMode(String ssoUrl, String entityId, String certificate, 
                                 String usernameAttribute, String emailAttribute, String defaultGroup) {
        selectSamlMode();
        configureSaml(ssoUrl, entityId, certificate, usernameAttribute, emailAttribute);
        setDefaultGroup(defaultGroup);
    }

    
    public void configureOAuth2Mode(String clientId, String clientSecret, String authUrl, 
                                   String tokenUrl, String userInfoUrl, String scope, String defaultGroup) {
        selectOAuth2Mode();
        configureOAuth2(clientId, clientSecret, authUrl, tokenUrl, userInfoUrl, scope);
        setDefaultGroup(defaultGroup);
    }
}