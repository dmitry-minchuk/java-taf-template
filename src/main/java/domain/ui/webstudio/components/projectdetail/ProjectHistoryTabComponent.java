package domain.ui.webstudio.components.projectdetail;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.BaseComponent;

import java.util.ArrayList;
import java.util.List;

public class ProjectHistoryTabComponent extends BaseComponent {

    private static final String AUTHOR_RELATIVE_TO_COMMENT = "xpath=../following-sibling::div[1]/span[1]";

    private final WebElement revisionEntries;

    public ProjectHistoryTabComponent(Page page) {
        this(new WebElement(page, "[data-testid=project-detail]", "projectDetail"));
    }

    public ProjectHistoryTabComponent(WebElement rootLocator) {
        super(rootLocator);
        revisionEntries = createScopedElement("xpath=.//*[starts-with(@data-testid,'revision-comment-')]", "revisionEntries");
    }

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

    public List<String> getRevisionAuthors() {
        Locator entries = revisionEntries.getLocator();
        entries.first().waitFor();
        List<String> authors = new ArrayList<>();
        int count = entries.count();
        for (int i = 0; i < count; i++) {
            authors.add(entries.nth(i).locator(AUTHOR_RELATIVE_TO_COMMENT).textContent().trim());
        }
        return authors;
    }

    public String getLatestRevisionId() {
        Locator first = revisionEntries.getLocator().first();
        first.waitFor();
        String testId = first.getAttribute("data-testid");
        return testId == null ? "" : testId.substring(testId.lastIndexOf('-') + 1);
    }

    public int getRevisionsCount() {
        revisionEntries.getLocator().first().waitFor();
        return revisionEntries.getLocator().count();
    }
}
