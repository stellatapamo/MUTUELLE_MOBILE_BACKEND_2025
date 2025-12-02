package com.mutuelle.mobille.enums;

public enum Role {
    ADMIN("ADMIN"),
    USER("MEMBER");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}