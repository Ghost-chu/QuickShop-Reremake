package org.maxgamer.quickshop.File;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.maxgamer.quickshop.QuickShopTest;

class JSONFileTest {

    private QuickShopTest plugin;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(QuickShopTest.class);
    }

    @Test
    void reload() {
        if (plugin == null) {
            return;
        }

        final IFile json = new JSONFile(plugin, "messages");

        json.create();
    }

    @Test
    void save() {
        if (plugin == null) {
            return;
        }


    }

    @AfterEach
    void tearDown() {
        MockBukkit.unload();
    }
}