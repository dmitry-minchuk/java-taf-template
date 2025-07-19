package domain.ui.webstudio.components.admincpmponents;

import configuration.core.ui.BasePageComponent;
import configuration.core.ui.SmartWebElement;
import helpers.utils.WaitUtil;
import org.openqa.selenium.support.FindBy;

import java.util.List;


public class RepositoriesPageComponent extends BasePageComponent {

    // Repository Type Selection Elements
    @FindBy(xpath = ".//div[contains(@class,'ant-tabs-tab') and contains(text(),'Design Repositories')]")
    private SmartWebElement designRepositoriesTab;

    @FindBy(xpath = ".//div[contains(@class,'ant-tabs-tab') and contains(text(),'Deploy Configuration Repository')]")
    private SmartWebElement deployConfigRepositoryTab;

    @FindBy(xpath = ".//div[contains(@class,'ant-tabs-tab') and contains(text(),'Deployment Repositories')]")
    private SmartWebElement deploymentRepositoriesTab;

    // Add Repository Button
    @FindBy(xpath = ".//button[./span[text()='Add Repository'] or ./span[contains(text(),'Add')]]")
    private SmartWebElement addRepositoryBtn;

    // Repository Form Elements
    @FindBy(xpath = ".//input[@placeholder='Name' or @id='repositoryName']")
    private SmartWebElement repositoryNameField;

    @FindBy(xpath = ".//select[contains(@id,'repositoryType')] | .//div[contains(@class,'ant-select') and ./preceding-sibling::*[contains(text(),'Type')]]")
    private SmartWebElement repositoryTypeDropdown;

    @FindBy(xpath = ".//div[contains(@class,'ant-select-item') and contains(text(),'Git')]")
    private SmartWebElement gitRepositoryTypeOption;

    @FindBy(xpath = ".//input[@placeholder='Remote repository' or contains(@id,'remoteRepository')]")
    private SmartWebElement remoteRepositoryField;

    @FindBy(xpath = ".//input[@placeholder='Local path' or contains(@id,'localPath')]")
    private SmartWebElement localPathField;

    @FindBy(xpath = ".//input[@placeholder='Protected branches' or contains(@id,'protectedBranches')]")
    private SmartWebElement protectedBranchesField;

    @FindBy(xpath = ".//input[@placeholder='New branch' or contains(@id,'newBranch')]")
    private SmartWebElement newBranchField;

    @FindBy(xpath = ".//input[@placeholder='Default branch name' or contains(@id,'defaultBranch')]")
    private SmartWebElement defaultBranchNameField;

    @FindBy(xpath = ".//input[@placeholder='Branch name pattern' or contains(@id,'branchPattern')]")
    private SmartWebElement branchNamePatternField;

    @FindBy(xpath = ".//input[@placeholder='Invalid branch name message hint' or contains(@id,'invalidBranchMessage')]")
    private SmartWebElement invalidBranchMessageField;

    // Structure Options
    @FindBy(xpath = ".//input[@type='checkbox' and (contains(@id,'flatStructure') or ./following-sibling::*[contains(text(),'Flat folder structure')])]")
    private SmartWebElement flatFolderStructureCheckbox;

    // Comments Section
    @FindBy(xpath = ".//input[@type='checkbox' and (contains(@id,'customizeComments') or ./following-sibling::*[contains(text(),'Customize comments')])]")
    private SmartWebElement customizeCommentsCheckbox;

    @FindBy(xpath = ".//textarea[@placeholder='Comment template' or contains(@id,'commentTemplate')]")
    private SmartWebElement commentTemplateField;

    // Repository List Elements
    @FindBy(xpath = ".//table//tbody[@class='ant-table-tbody']")
    private SmartWebElement repositoryTableBody;

    @FindBy(xpath = ".//table//tbody[@class='ant-table-tbody']//tr[@class='ant-table-row ant-table-row-level-0']")
    private List<SmartWebElement> repositoryRows;

    // Repository Row Template Elements (for dynamic repository interaction)
    @FindBy(xpath = ".//tr[contains(@data-row-key,'%s') or .//td[contains(text(),'%s')]]")
    private SmartWebElement repositoryRowTemplate;

    @FindBy(xpath = ".//tr[contains(@data-row-key,'%s') or .//td[contains(text(),'%s')]]//button[contains(@class,'edit') or ./span[contains(@aria-label,'edit')]]")
    private SmartWebElement editRepositoryBtnTemplate;

    @FindBy(xpath = ".//tr[contains(@data-row-key,'%s') or .//td[contains(text(),'%s')]]//button[contains(@class,'delete') or ./span[contains(@aria-label,'delete')]]")
    private SmartWebElement deleteRepositoryBtnTemplate;

    // Action Buttons
    @FindBy(xpath = ".//button[./span[text()='Apply Changes'] or ./span[text()='Apply']]")
    private SmartWebElement applyChangesBtn;

    @FindBy(xpath = ".//button[./span[text()='Save Repository'] or ./span[text()='Save']]")
    private SmartWebElement saveRepositoryBtn;

    @FindBy(xpath = ".//button[./span[text()='Cancel']]")
    private SmartWebElement cancelBtn;

    @FindBy(xpath = ".//button[./span[text()='Test Connection']]")
    private SmartWebElement testConnectionBtn;


    // Status and Notification Elements
    @FindBy(xpath = ".//div[contains(@class,'ant-notification') or contains(@class,'success-message')]")
    private SmartWebElement successNotification;

    @FindBy(xpath = ".//div[contains(@class,'ant-notification') or contains(@class,'error-message')]")
    private SmartWebElement errorNotification;

    // Repository Type Navigation Methods
    
    public void switchToDesignRepositories() {
        designRepositoriesTab.click();
    }

    
    public void switchToDeployConfigRepository() {
        deployConfigRepositoryTab.click();
    }

    
    public void switchToDeploymentRepositories() {
        deploymentRepositoriesTab.click();
    }

    // Repository Management Methods
    
    public void clickAddRepository() {
        addRepositoryBtn.click();
    }

    
    public int getRepositoryCount() {
        return repositoryRows.size();
    }

    
    public boolean isRepositoryExists(String repositoryName) {
        return repositoryRowTemplate.format(repositoryName, repositoryName).isDisplayed(2);
    }

    
    public void clickEditRepository(String repositoryName) {
        editRepositoryBtnTemplate.format(repositoryName, repositoryName).click();
    }

    
    public void clickDeleteRepository(String repositoryName) {
        deleteRepositoryBtnTemplate.format(repositoryName, repositoryName).click();
    }

    // Repository Form Methods
    
    public void setRepositoryName(String name) {
        repositoryNameField.sendKeys(name);
    }

    
    public void setRepositoryTypeToGit() {
        repositoryTypeDropdown.click();
        gitRepositoryTypeOption.click();
    }

    
    public void setRemoteRepository(String remoteUrl) {
        remoteRepositoryField.sendKeys(remoteUrl);
    }

    
    public void setLocalPath(String localPath) {
        localPathField.sendKeys(localPath);
    }

    
    public void setProtectedBranches(String protectedBranches) {
        protectedBranchesField.sendKeys(protectedBranches);
    }

    
    public void setNewBranch(String newBranch) {
        newBranchField.sendKeys(newBranch);
    }

    
    public void setDefaultBranchName(String defaultBranch) {
        defaultBranchNameField.sendKeys(defaultBranch);
    }

    
    public void setBranchNamePattern(String branchPattern) {
        branchNamePatternField.sendKeys(branchPattern);
    }

    
    public void setInvalidBranchMessage(String invalidMessage) {
        invalidBranchMessageField.sendKeys(invalidMessage);
    }

    
    public String getRepositoryName() {
        return repositoryNameField.getAttribute("value");
    }

    
    public String getRemoteRepository() {
        return remoteRepositoryField.getAttribute("value");
    }

    
    public String getLocalPath() {
        return localPathField.getAttribute("value");
    }

    // Structure and Options Methods
    
    public void setFlatFolderStructure(boolean flatStructure) {
        if (flatStructure != flatFolderStructureCheckbox.isSelected()) {
            flatFolderStructureCheckbox.click();
        }
    }

    
    public boolean isFlatFolderStructureEnabled() {
        return flatFolderStructureCheckbox.isSelected();
    }

    
    public void setCustomizeComments(boolean customize) {
        if (customize != customizeCommentsCheckbox.isSelected()) {
            customizeCommentsCheckbox.click();
        }
    }

    
    public boolean isCustomizeCommentsEnabled() {
        return customizeCommentsCheckbox.isSelected();
    }

    
    public void setCommentTemplate(String commentTemplate) {
        commentTemplateField.sendKeys(commentTemplate);
    }

    
    public String getCommentTemplate() {
        return commentTemplateField.getAttribute("value");
    }

    // Action Methods
    
    public void applyChanges() {
        applyChangesBtn.click();
    }

    
    public void saveRepository() {
        saveRepositoryBtn.click();
    }

    
    public void cancelRepository() {
        cancelBtn.click();
    }

    
    public void testConnection() {
        testConnectionBtn.click();
    }

    
    public void confirmAction() {
        getConfirmationPopup().confirm();
    }

    
    public void cancelAction() {
        getConfirmationPopup().cancel();
    }

    // Status and Notification Methods
    
    public boolean isSuccessNotificationDisplayed() {
        return successNotification.isDisplayed(3);
    }

    
    public boolean isErrorNotificationDisplayed() {
        return errorNotification.isDisplayed(3);
    }

    
    public String getSuccessNotificationMessage() {
        return successNotification.getText();
    }

    
    public String getErrorNotificationMessage() {
        return errorNotification.getText();
    }

    // Complex Repository Operations
    
    public void createGitRepository(String name, String remoteUrl, String localPath) {
        clickAddRepository();
        setRepositoryName(name);
        setRepositoryTypeToGit();
        setRemoteRepository(remoteUrl);
        setLocalPath(localPath);
        saveRepository();
    }

    
    public void createGitRepositoryWithFullConfig(RepositoryConfig config) {
        clickAddRepository();
        setRepositoryName(config.name);
        setRepositoryTypeToGit();
        setRemoteRepository(config.remoteUrl);
        setLocalPath(config.localPath);
        
        if (config.protectedBranches != null) {
            setProtectedBranches(config.protectedBranches);
        }
        if (config.defaultBranch != null) {
            setDefaultBranchName(config.defaultBranch);
        }
        if (config.branchPattern != null) {
            setBranchNamePattern(config.branchPattern);
        }
        setFlatFolderStructure(config.flatStructure);
        saveRepository();
    }


    
    public void editRepository(String repositoryName, String newRemoteUrl, String newLocalPath) {
        clickEditRepository(repositoryName);
        if (newRemoteUrl != null) {
            setRemoteRepository(newRemoteUrl);
        }
        if (newLocalPath != null) {
            setLocalPath(newLocalPath);
        }
        saveRepository();
    }

    
    public void deleteRepository(String repositoryName) {
        clickDeleteRepository(repositoryName);
        confirmAction();
    }

    
    public void cancelDeleteRepository(String repositoryName) {
        clickDeleteRepository(repositoryName);
        cancelAction();
    }

    // Repository Validation Methods
    
    public boolean validateRepository(String repositoryName, String expectedRemoteUrl, String expectedLocalPath) {
        if (!isRepositoryExists(repositoryName)) {
            return false;
        }
        
        clickEditRepository(repositoryName);
        boolean remoteMatches = expectedRemoteUrl == null || expectedRemoteUrl.equals(getRemoteRepository());
        boolean localPathMatches = expectedLocalPath == null || expectedLocalPath.equals(getLocalPath());
        cancelRepository();
        
        return remoteMatches && localPathMatches;
    }

    
    public String getRepositoryInfo(String repositoryName) {
        if (!isRepositoryExists(repositoryName)) {
            return "Repository not found: " + repositoryName;
        }
        
        clickEditRepository(repositoryName);
        String info = String.format("Repository: %s | Remote: %s | Local: %s | Flat Structure: %s",
                getRepositoryName(),
                getRemoteRepository(),
                getLocalPath(),
                isFlatFolderStructureEnabled());
        cancelRepository();
        
        return info;
    }

    
    public static class RepositoryConfig {
        public final String name;
        public final String remoteUrl;
        public final String localPath;
        public final String protectedBranches;
        public final String defaultBranch;
        public final String branchPattern;
        public final boolean flatStructure;

        private RepositoryConfig(Builder builder) {
            this.name = builder.name;
            this.remoteUrl = builder.remoteUrl;
            this.localPath = builder.localPath;
            this.protectedBranches = builder.protectedBranches;
            this.defaultBranch = builder.defaultBranch;
            this.branchPattern = builder.branchPattern;
            this.flatStructure = builder.flatStructure;
        }

        public static class Builder {
            private final String name;
            private final String remoteUrl;
            private final String localPath;
            private String protectedBranches;
            private String defaultBranch;
            private String branchPattern;
            private boolean flatStructure = false;

            public Builder(String name, String remoteUrl, String localPath) {
                this.name = name;
                this.remoteUrl = remoteUrl;
                this.localPath = localPath;
            }

            public Builder protectedBranches(String protectedBranches) {
                this.protectedBranches = protectedBranches;
                return this;
            }

            public Builder defaultBranch(String defaultBranch) {
                this.defaultBranch = defaultBranch;
                return this;
            }

            public Builder branchPattern(String branchPattern) {
                this.branchPattern = branchPattern;
                return this;
            }

            public Builder flatStructure(boolean flatStructure) {
                this.flatStructure = flatStructure;
                return this;
            }

            public RepositoryConfig build() {
                return new RepositoryConfig(this);
            }
        }
    }
}