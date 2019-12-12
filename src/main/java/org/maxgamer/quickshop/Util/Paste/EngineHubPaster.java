package org.maxgamer.quickshop.Util.Paste;

import org.jetbrains.annotations.NotNull;

public class EngineHubPaster implements PasteInterface {
    @Override
    public String pasteTheText(@NotNull String text) throws Exception {
        org.maxgamer.quickshop.NonQuickShopStuffs.com.sk89q.worldedit.util.paste.EngineHubPaster paster = new org.maxgamer.quickshop.NonQuickShopStuffs.com.sk89q.worldedit.util.paste.EngineHubPaster();
        return paster.paste(text).call().toString();
    }
}
