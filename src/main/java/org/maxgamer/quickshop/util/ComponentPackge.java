package org.maxgamer.quickshop.util;

import net.md_5.bungee.api.chat.BaseComponent;

public class ComponentPackge {
    private final BaseComponent[] components;

    public ComponentPackge(BaseComponent... components) {
        this.components = components;
    }

    public BaseComponent[] getComponents() {
        return components;
    }
}
