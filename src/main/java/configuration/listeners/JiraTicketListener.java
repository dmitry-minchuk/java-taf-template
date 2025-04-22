package configuration.listeners;

import configuration.annotations.JiraTicket;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class JiraTicketListener implements ITestListener {

    private static final String JIRA_BASE_URL = "https://jira.eisgroup.com/browse/";

    @Override
    public void onTestStart(ITestResult result) {
        Method method = result.getMethod().getConstructorOrMethod().getMethod();
        if (method.isAnnotationPresent(JiraTicket.class)) {
            JiraTicket jiraTicket = method.getAnnotation(JiraTicket.class);
            String ticketId = jiraTicket.value();

            Object existing = result.getAttribute("rp.attributes");

            Map<String, String> attributes;
            if (existing instanceof Map) {
                attributes = new HashMap<>((Map<String, String>) existing);
            } else {
                attributes = new HashMap<>();
            }

            String jiraLink = JIRA_BASE_URL + ticketId;
            attributes.put("jira", jiraLink);
            result.setAttribute("rp.attributes", attributes);
        }
    }
}
