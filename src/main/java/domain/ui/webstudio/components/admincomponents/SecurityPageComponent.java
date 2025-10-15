package domain.ui.webstudio.components.admincomponents;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;
import lombok.Getter;

@Getter
public class SecurityPageComponent extends BaseComponent {

    // User Mode Radio Buttons
    private WebElement singleUserModeRadio;
    private WebElement multiUserModeRadio;
    private WebElement activeDirectoryModeRadio;
    private WebElement samlModeRadio;
    private WebElement oauth2ModeRadio;

    // Multi-User Mode Fields
    private WebElement administratorsField;
    private WebElement defaultGroupDropdown;
    private WebElement defaultGroupDropdownList;

    // Buttons
    private WebElement applyBtn;

    public SecurityPageComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public SecurityPageComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        // User Mode Radio Buttons
        singleUserModeRadio = createScopedElement("xpath=.//input[@type='radio' and @value='single']", "singleUserModeRadio");
        multiUserModeRadio = createScopedElement("xpath=.//input[@type='radio' and @value='multi']", "multiUserModeRadio");
        activeDirectoryModeRadio = createScopedElement("xpath=.//input[@type='radio' and @value='ad']", "activeDirectoryModeRadio");
        samlModeRadio = createScopedElement("xpath=.//input[@type='radio' and @value='saml']", "samlModeRadio");
        oauth2ModeRadio = createScopedElement("xpath=.//input[@type='radio' and @value='oauth2']", "oauth2ModeRadio");

        // Multi-User Mode Fields (visible when multi-user mode is selected)
        administratorsField = createScopedElement("xpath=.//input[@id='administrators']", "administratorsField");
        defaultGroupDropdown = createScopedElement("xpath=.//div[./label[@title='Default Group']]/following-sibling::div//div[@class='ant-form-item-control-input-content']", "defaultGroupDropdown");
        defaultGroupDropdownList = new WebElement(page, "xpath=//div[@class='rc-virtual-list-holder']//div[contains(@class,'ant-select-item-option') and @title='%s']", "defaultGroupDropdownList");

        // Buttons
        applyBtn = createScopedElement("xpath=.//button[./span[text()='Apply']]", "applyBtn");
    }

    public void clickApply() {
        applyBtn.click();
        getModalOkBtn().click();
        WaitUtil.sleep(1000, "Waiting for security settings to be applied");
    }

    public enum DefaultGroup {
        NONE("None"),
        ADMINISTRATORS("Administrators"),
        ANALYSTS("Analysts"),
        DEPLOYERS("Deployers"),
        DEVELOPERS("Developers"),
        TESTERS("Testers"),
        VIEWERS("Viewers");

        private final String value;

        DefaultGroup(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static DefaultGroup fromValue(String value) {
            for (DefaultGroup group : values()) {
                if (group.getValue().equalsIgnoreCase(value)) {
                    return group;
                }
            }
            return null;
        }
    }

    public SecurityPageComponent selectDefaultGroup(String value) {
        defaultGroupDropdown.click();
        defaultGroupDropdownList.format(value).waitForVisible(500).click();
        return this;
    }
}