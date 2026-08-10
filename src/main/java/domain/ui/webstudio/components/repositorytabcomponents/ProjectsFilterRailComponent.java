package domain.ui.webstudio.components.repositorytabcomponents;

import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.BaseComponent;

/**
 * The filter rail of the React projects list (Studio 6.4.0): collapsible facet groups with repository and
 * status checkboxes. Scoped under {@code [data-testid=projects-rail]} (verified against the live DOM).
 */
public class ProjectsFilterRailComponent extends BaseComponent {

    public static final String REPOSITORY_FILTER_GROUP = "repository";
    public static final String STATUS_FILTER_GROUP = "status";

    // Facet rows render with the rail itself, so a short probe decides present-vs-absent.
    private static final int PROBE_MS = DEFAULT_TIMEOUT_MS / 5;

    private final WebElement filterGroupToggle;
    private final WebElement filterGroupShow;
    private final WebElement filterRepoCheckbox;
    private final WebElement filterStatusCheckbox;

    public ProjectsFilterRailComponent(Page page) {
        this(new WebElement(page, "[data-testid=projects-rail]", "projectsRail"));
    }

    public ProjectsFilterRailComponent(WebElement rootLocator) {
        super(rootLocator);
        filterGroupToggle = createScopedElement("[data-testid=filter-toggle-%s]", "filterGroupToggle");
        filterGroupShow = createScopedElement("[data-testid=filter-show-%s]", "filterGroupShow");
        filterRepoCheckbox = createScopedElement("[data-testid=filter-repo-%s]", "filterRepoCheckbox");
        filterStatusCheckbox = createScopedElement("[data-testid=filter-status-%s]", "filterStatusCheckbox");
    }

    /**
     * Opens a filter-rail group so its checkboxes exist. Studio 6.4.0 lets each group be collapsed (its rows
     * are then absent from the DOM, not merely hidden) and even hidden away behind a "show" button.
     */
    public void expandFilterGroup(String groupId) {
        WebElement showBtn = filterGroupShow.format(groupId);
        if (showBtn.isVisible(PROBE_MS)) {
            showBtn.click();
        }
        WebElement toggle = filterGroupToggle.format(groupId);
        if (toggle.isVisible(PROBE_MS) && "false".equals(toggle.getAttribute("aria-expanded"))) {
            toggle.click();
        }
    }

    // The React projects list is repo-filtered via checkboxes in the filter rail (filter-repo-<name>, lowercase).
    // Ensures the given repository's projects are shown (checks the box only if not already checked).
    public void ensureRepoFilterChecked(String repositoryNameLower) {
        expandFilterGroup(REPOSITORY_FILTER_GROUP);
        WebElement checkbox = filterRepoCheckbox.format(repositoryNameLower);
        if (checkbox.isVisible(PROBE_MS) && !checkbox.isChecked()) {
            checkbox.click();
            waitUntilSpinnerLoaded();
        }
    }

    /**
     * Ticks (or unticks) a status facet — LOCAL / OPENED / EDITING / VIEWING_VERSION / CLOSED / DELETED.
     * A status no project is currently in is not offered at all, so this is a no-op for an absent facet.
     */
    public void setStatusFilter(String status, boolean checked) {
        expandFilterGroup(STATUS_FILTER_GROUP);
        WebElement checkbox = filterStatusCheckbox.format(status);
        if (checkbox.isVisible(PROBE_MS) && checkbox.isChecked() != checked) {
            checkbox.click();
            waitUntilSpinnerLoaded();
        }
    }
}
