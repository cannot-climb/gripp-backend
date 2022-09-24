package kr.njw.gripp.global.auth;

public enum Authority {
    USER("USER");

    private final String value;

    Authority(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
