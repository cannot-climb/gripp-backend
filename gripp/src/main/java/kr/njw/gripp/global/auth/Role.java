package kr.njw.gripp.global.auth;

public enum Role {
    USER("USER"),
    ADMIN("ADMIN");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public String toAuthority() {
        return "ROLE_" + this.value;
    }
}
