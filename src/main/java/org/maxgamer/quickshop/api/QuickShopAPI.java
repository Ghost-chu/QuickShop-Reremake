/*
 * MIT License
 *
 * Copyright © 2020 Bukkit Commons Studio
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.maxgamer.quickshop.api;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Shop;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class QuickShopAPI {

    private QuickShopAPI() {
    }

    @NotNull
    public static Optional<Shop> getShopByShopUUID(@NotNull UUID shopUuid) {
        return Optional.empty();
    }

    @NotNull
    public static Optional<Shop> getShopByPlayerUUID(@NotNull UUID playerUuid) {
        return Optional.empty();
    }

    @NotNull
    public static List<Shop> getShopsOf(@NotNull Player player) {
        return Collections.emptyList();
    }

    @NotNull
    public static List<Shop> getShops() {
        return Collections.emptyList();
    }

    @NotNull
    public static List<Shop> getLoadedShops(@Nullable World world) {
        return Collections.emptyList();
    }


}
