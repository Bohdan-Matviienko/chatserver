package org.example.enums;

public enum Command {
    LOGIN("/$login"),
    LOGIN_OK("/$logok"),
    LOGIN_FAIL("/$logfail"),
    REGISTER("/$register"),
    REG_OK("/$regok"),
    REG_FAIL("/$regfail"),
    PRIVATE("/$private"),
    USER_LIST("/$userlist"),
    FINISH("/$finish");

    private final String value;

    Command(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
