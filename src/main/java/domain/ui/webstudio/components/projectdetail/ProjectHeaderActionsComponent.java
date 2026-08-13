package domain.ui.webstudio.components.projectdetail;

import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.BaseComponent;

public class ProjectHeaderActionsComponent extends BaseComponent {

    private static final int PROBE_MS = DEFAULT_TIMEOUT_MS / 5;

    private final WebElement actionByLabel;
    private final WebElement moreBtn;
    private final WebElement overflowItem;

    public ProjectHeaderActionsComponent(Page page) {
        this(new WebElement(page, "[data-testid=project-actions]", "projectHeaderActions"));
    }

    public ProjectHeaderActionsComponent(WebElement rootLocator) {
        super(rootLocator);
        actionByLabel = createScopedElement("xpath=.//button[starts-with(@data-testid,'%s-')]", "headerAction");
        moreBtn = createScopedElement("[data-testid=project-actions-more]", "headerMoreBtn");
        overflowItem = new WebElement(page, "xpath=//div[contains(@class,'ant-dropdown')][not(contains(@class,'ant-dropdown-hidden'))]//button[normalize-space()='%s']", "headerOverflowItem");
    }

    public void clickAction(String actionLabel) {
        WebElement action = actionByLabel.format(actionIdOf(actionLabel));
        if (action.isVisible(PROBE_MS)) {
            LOGGER.info("Header action '{}' clicked on the bar", actionLabel);
            action.click();
            return;
        }
        LOGGER.info("Header action '{}' is not on the bar; opening the Actions menu", actionLabel);
        moreBtn.click();
        overflowItem.format(actionLabel).click();
    }

    public boolean isActionAvailable(String actionLabel) {
        WebElement action = actionByLabel.format(actionIdOf(actionLabel));
        if (action.isVisible(PROBE_MS)) {
            return true;
        }
        if (!moreBtn.isVisible(PROBE_MS)) {
            return false;
        }
        moreBtn.click();
        boolean inOverflow = overflowItem.format(actionLabel).isVisible(PROBE_MS);
        page.keyboard().press("Escape");
        return inOverflow;
    }

    private static String actionIdOf(String actionLabel) {
        return switch (actionLabel) {
            case "Open Revision" -> "openRevision";
            case "Delete Branch" -> "deleteBranch";
            default -> actionLabel.substring(0, 1).toLowerCase() + actionLabel.substring(1);
        };
    }
}
