package domain.ui.webstudio.components.editortabcomponents.toolbar;

import java.util.List;

/**
 * The Trace popup window. EPBDS-16195 reworked Trace into an interactive step debugger. The debugger
 * opens SUSPENDED (paused at the start); the call tree, traced table and node details are only populated
 * while suspended. Resuming to the end ("Completed") clears everything, so inspection is done in the
 * suspended state.
 */
public interface ITraceWindow {
    String getStatus();
    List<String> getCallTreeTitles();
    ITraceWindow selectTreeNode(int position);
    ITraceWindow stepOver();
    ITraceWindow resume();
    String getDetailsText();
    String getTracedTableText();
    boolean isNodeDetailsErrorDisplayed(int timeoutInMillis);
    boolean areDetailsDisplayed(int timeoutInMillis);
    void close();
}
