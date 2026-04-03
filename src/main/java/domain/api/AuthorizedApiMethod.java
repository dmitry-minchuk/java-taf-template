package domain.api;

import com.microsoft.playwright.options.Cookie;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.serviceclasses.models.UserData;
import helpers.service.UserService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.List;

public abstract class AuthorizedApiMethod extends ApiBaseMethod {

    protected AuthorizedApiMethod(String path) {
        super(path);
    }

    protected RequestSpecification authorizedRequest() {
        RequestSpecification specification = RestAssured.given();

        List<Cookie> cookies = extractBrowserSessionCookies();
        if (!cookies.isEmpty()) {
            cookies.forEach(cookie -> specification.cookie(cookie.name, cookie.value));
            return specification;
        }

        UserData admin = UserService.getUser(User.ADMIN);
        return specification.auth()
                .preemptive()
                .basic(admin.getLogin(), admin.getPassword());
    }

    protected RequestSpecification authorizedJsonRequest(Object body) {
        return authorizedRequest()
                .contentType(ContentType.JSON)
                .body(body);
    }

    private List<Cookie> extractBrowserSessionCookies() {
        try {
            if (LocalDriverPool.getPage() == null) {
                return List.of();
            }
            return LocalDriverPool.getPage().context().cookies();
        } catch (Exception e) {
            LOGGER.debug("Unable to read Playwright session cookies, falling back to basic auth: {}", e.getMessage());
            return List.of();
        }
    }
}
