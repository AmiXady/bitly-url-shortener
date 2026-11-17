package kz.amixady.link.links.entity;

public enum LinkStatus {

    ACTIVE("ACTIVE"),
    ARCHIVED("ARCHIVED"),
    DELETED("DELETED");

    private final String value;

    LinkStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}