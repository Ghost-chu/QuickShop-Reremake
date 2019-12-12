package org.maxgamer.quickshop.File;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.llorllale.cactoos.matchers.Assertion;
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
        new Assertion<>(
            "There is no 'signs.item' path",
            yaml.get("signs.item"),
            new IsEqual<>("{0}")
        ).affirm();
    }

    @Test
    void set() {
        yaml.set("test.test", "test");
        new Assertion<>(
            "Cannot be set 'test.test' path as 'test'",
            yaml.get("test.test"),
            new IsEqual<>("test")
        ).affirm();
    }

    @AfterEach
    void stop() {
        yaml.save();
        MockBukkit.unload();
    }

}