package org.maxgamer.quickshop.Shop;

import lombok.ToString;
import org.jetbrains.annotations.NotNull;
@ToString
public enum MatcherWorkMode {
    QSMATCHER(0), BUKKITMATCHER(1), JAVAMATCHER(2);

    public static @NotNull MatcherWorkMode fromID(int id) {
        for (MatcherWorkMode workMode : MatcherWorkMode.values()) {
            if (workMode.id == id) {
                return workMode;
            }
        }
        return QSMATCHER;
    }

    public static int toID(@NotNull MatcherWorkMode workMode) {
        return workMode.id;
    }

    MatcherWorkMode(int id) {
        this.id = id;
    }

    private int id;

    public int toID() {
        return id;
    }

    public int getId() {
        return id;
    }
}
