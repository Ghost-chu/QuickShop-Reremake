package org.maxgamer.quickshop.File;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.llorllale.cactoos.matchers.Assertion;
import org.maxgamer.quickshop.QuickShopTest;

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
        new Assertion<>(
            "There is no 'signs.item' path",
            json.get("signs.item"),
            new IsEqual<>("{0}")
        ).affirm();
    }

    @AfterEach
    void stop() {
        MockBukkit.unload();
    }

}