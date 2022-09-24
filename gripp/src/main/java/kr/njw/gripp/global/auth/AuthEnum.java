package kr.njw.gripp.global.auth;

public enum AuthEnum {
    AUTHORITY_USER("USER");

    private final String value;

    AuthEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
