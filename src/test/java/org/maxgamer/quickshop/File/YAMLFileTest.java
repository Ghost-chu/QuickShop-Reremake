package org.maxgamer.quickshop.File;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.maxgamer.quickshop.QuickShopTest;

import static org.junit.jupiter.api.Assertions.*;

final class YAMLFileTest {

    private QuickShopTest plugin;
    private IFile yaml;

    @BeforeEach
    void start() {
        MockBukkit.mock();
        plugin = MockBukkit.load(QuickShopTest.class);
        yaml = new YAMLFile(plugin, "messages");

        yaml.create();
    }

    @Test
    void get() {
        assertEquals(
            yaml.get("signs.item"),
            "{0}"
        );
    }

    @Test
    void set() {
        yaml.set("test.test", "test");
        assertEquals(
            yaml.get("test.test"),
            "test"
        );
    }

    @AfterEach
    void stop() {
        yaml.save();
        MockBukkit.unload();
    }

}