package domain.serviceclasses.constants;

public enum User {
    ADMIN("admin"),
    OPENL_1("openl_1"),
    OPENL_2("openl_2"),
    OPENL_AC("openl_ac");

    private String value;

    User(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
