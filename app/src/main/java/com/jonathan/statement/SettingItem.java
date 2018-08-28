package com.jonathan.statement;

public class SettingItem {
    String name;
    int icon_uri;

    public SettingItem(String name, int icon_uri) {
        this.name = name;
        this.icon_uri = icon_uri;
    }

    public String getName() {
        return name;
    }

    public int getIcon_uri() {
        return icon_uri;
    }
}
