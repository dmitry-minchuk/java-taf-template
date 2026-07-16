package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;

// React "Sync updates" dialog (build 032c60a664ce+), opened from the project-detail Branches tab via a
// branch row's Merge action. Replaces the legacy SyncChangesDialogComponent. The merge target is implicit
// (the branch whose Merge action was clicked) — there is no branch dropdown. Two actions: "Receive their
// updates" (import) and "Send your updates" (export), each disabled when the branches are already in sync.
public class SyncUpdatesDialogComponent extends BaseComponent {

    private static final String MODAL_ROOT =
            "//div[contains(@class,'ant-modal')][.//div[contains(@class,'ant-modal-title') and contains(normalize-space(),'Sync updates')]]";

    private WebElement modalTitle;
    private WebElement receiveBtn;
    private WebElement sendBtn;
    private WebElement closeBtn;

    public SyncUpdatesDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    private void initializeElements() {
        modalTitle = new WebElement(LocalDriverPool.getPage(),
                "xpath=" + MODAL_ROOT + "//div[contains(@class,'ant-modal-title')]", "syncUpdatesTitle");
        receiveBtn = new WebElement(LocalDriverPool.getPage(),
                "xpath=" + MODAL_ROOT + "//button[.//span[normalize-space()='Receive their updates'] or normalize-space()='Receive their updates']", "syncReceiveBtn");
        sendBtn = new WebElement(LocalDriverPool.getPage(),
                "xpath=" + MODAL_ROOT + "//button[.//span[normalize-space()='Send your updates'] or normalize-space()='Send your updates']", "syncSendBtn");
        closeBtn = new WebElement(LocalDriverPool.getPage(),
                "xpath=" + MODAL_ROOT + "//button[contains(@class,'ant-modal-close')]", "syncCloseBtn");
    }

    public SyncUpdatesDialogComponent waitForVisible() {
        receiveBtn.waitForVisible();
        return this;
    }

    public String getHeader() {
        return modalTitle.getText().trim();
    }

    // "Receive their updates" = pull the target branch's changes into the current branch (legacy import).
    public boolean isReceiveEnabled() {
        return receiveBtn.isEnabled();
    }

    // "Send your updates" = push the current branch's changes to the target branch (legacy export).
    public boolean isSendEnabled() {
        return sendBtn.isEnabled();
    }

    public void clickReceive() {
        receiveBtn.click();
    }

    public void clickSend() {
        sendBtn.click();
    }

    public void close() {
        closeBtn.click();
    }
}
