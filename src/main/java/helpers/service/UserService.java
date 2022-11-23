package helpers.service;

import configuration.ProjectConfiguration;
import domain.serviceclasses.constants.User;
import domain.serviceclasses.models.UserData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class UserService {
    protected static final Logger LOGGER = LogManager.getLogger(UserService.class);
    private static final String USER_POOL = "user_pool";
    private static final String SPLITTER = "::";
    private static final String USER_NAME_DATA_PATTERN = "%s.login";
    private static final String USER_PASSWORD_DATA_PATTERN = "%s.password";
    private static final List<UserData> userList = new ArrayList<>();

    public static UserData getUser(User user) {
        if (userList.isEmpty()) {
            initUserList();
        }
        return userList.stream().filter(u -> u.getLogin().equals(user.getValue())).findFirst().orElseThrow(
                () -> new RuntimeException("No such user in the pool: " + user.getValue()));
    }

    private static void initUserList() {
        String userPool = ProjectConfiguration.getProperty(USER_POOL);
        LOGGER.debug("User pool initialization. User: " + userPool);
        if (userPool.isEmpty()) {
            throw new RuntimeException("User pool is empty!");
        }
        String[] users = userPool.split(SPLITTER);
        for (String user : users) {
            String userName = ProjectConfiguration.getProperty(String.format(USER_NAME_DATA_PATTERN, user));
            String userPassword = ProjectConfiguration.getProperty(String.format(USER_PASSWORD_DATA_PATTERN, user));
            userList.add(new UserData(userName, userPassword));
        }
    }
}
