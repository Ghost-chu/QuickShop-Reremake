package org.maxgamer.quickshop.File;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.maxgamer.quickshop.QuickShopTest;

class JSONFileTest {

    private QuickShopTest plugin;
    private IFile json;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(QuickShopTest.class);
        json = new JSONFile(plugin, "messages");
        json.create();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unload();
    }

}
