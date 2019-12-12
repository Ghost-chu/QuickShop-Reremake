package org.maxgamer.quickshop.File;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.maxgamer.quickshop.QuickShopTest;

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
    }

    @AfterEach
    void stop() {
        MockBukkit.unload();
    }

}