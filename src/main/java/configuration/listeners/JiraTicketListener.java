package configuration.listeners;

import configuration.annotations.JiraTicket;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.lang.reflect.Method;

public class JiraTicketListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        Method method = result.getMethod().getConstructorOrMethod().getMethod();
        if (method.isAnnotationPresent(JiraTicket.class)) {
            JiraTicket jiraTicket = method.getAnnotation(JiraTicket.class);
            String ticketId = jiraTicket.value();
            // here ticketId can be passed into Report Portal or other reporting tool
        }
    }
}
