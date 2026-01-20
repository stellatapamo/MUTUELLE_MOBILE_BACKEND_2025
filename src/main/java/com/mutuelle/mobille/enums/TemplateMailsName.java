package com.mutuelle.mobille.enums;

public enum TemplateMailsName {
    WELCOME("welcome"),
    PLAFOND_DEPASSE_ADMIN("alerte-plafond-depasse-admin"),
    PLAFOND_DEPASSE_MEMBER("alerte-plafond-depasse-membre"),
    SESSION_STARTED("session-start"),
    SESSION_ENDED("alerte-end"),
    SOLIDARITY_ADDED("solidarity-add");

    private final String value;

    TemplateMailsName(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
