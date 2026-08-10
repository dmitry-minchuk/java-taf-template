package domain.ui.webstudio.components.projectdetail;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.BaseComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * The History tab of the React project-detail view: the revision list (one {@code revision-comment-<hash>}
 * entry per revision, newest first). Scoped under {@code [data-testid=project-detail]}. Callers open the
 * tab first — see {@code ProjectDetailPage.openHistoryTab()}.
 */
public class ProjectHistoryTabComponent extends BaseComponent {

    private final WebElement revisionEntries;

    public ProjectHistoryTabComponent(Page page) {
        this(new WebElement(page, "[data-testid=project-detail]", "projectDetail"));
    }

    public ProjectHistoryTabComponent(WebElement rootLocator) {
        super(rootLocator);
        revisionEntries = createScopedElement("xpath=.//*[starts-with(@data-testid,'revision-comment-')]", "revisionEntries");
    }

    // Revision comments, newest first. Replaces the legacy
    // RepositoryContentRevisionsTabComponent.getRevisionDescription(i) loop.
    public List<String> getRevisionDescriptions() {
        Locator entries = revisionEntries.getLocator();
        entries.first().waitFor();
        List<String> descriptions = new ArrayList<>();
        int count = entries.count();
        for (int i = 0; i < count; i++) {
            descriptions.add(entries.nth(i).textContent().trim());
        }
        return descriptions;
    }

    // The newest revision's git hash, parsed from the first entry's data-testid
    // ("revision-comment-<hash>"). Lets callers assert a new revision was committed (id changes).
    public String getLatestRevisionId() {
        Locator first = revisionEntries.getLocator().first();
        first.waitFor();
        String testId = first.getAttribute("data-testid");
        return testId == null ? "" : testId.substring(testId.lastIndexOf('-') + 1);
    }

    public int getRevisionsCount() {
        // The revision list loads asynchronously after the spinner clears, so wait for the first entry to
        // render before counting (every project has at least the creation revision). waitForVisible would
        // trip strict mode on the multi-match, so wait on the first entry directly.
        revisionEntries.getLocator().first().waitFor();
        return revisionEntries.getLocator().count();
    }
}
