package org.maxgamer.quickshop.File;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.maxgamer.quickshop.QuickShopTest;
import static org.junit.jupiter.api.Assertions.*;

final class JSONFileTest {

    private QuickShopTest plugin;
    private IFile json;

    @BeforeEach
    void start() {
        MockBukkit.mock();
        plugin = MockBukkit.load(QuickShopTest.class);
        json = new JSONFile(plugin, "messages");
        json.create();
    }

    @Test
    void get() {
        assertEquals(
            json.get("signs.item"),
            "{0}"
        );
    }

    @Test
    void set() {
        json.set("test.test", "test");
        assertEquals(
            json.get("signs.item"),
            "{0}"
        );
    }

    @AfterEach
    void stop() {
        json.save();
        MockBukkit.unload();
    }

}