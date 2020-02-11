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

package org.maxgamer.quickshop.handle;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShopLoader;
import org.maxgamer.quickshop.handle.abs.Registry;
import org.maxgamer.quickshop.handle.shoptypes.abs.ShopType;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public final class RegistryBasic implements Registry {

    private final Map<String, ShopType> shopTypes = new ConcurrentHashMap<>();

    @NotNull
    private final QuickShopLoader loader;

    @Override
    public void registerShopType(@NotNull String id, @NotNull ShopType shopType) {
        shopTypes.put(id, shopType);
    }

    @NotNull
    @Override
    public Optional<ShopType> getShopTypeById(@NotNull String id) {
        return Optional.ofNullable(shopTypes.get(id));
    }

    @NotNull
    @Override
    public Collection<ShopType> getShopTypes() {
        return Collections.unmodifiableCollection(shopTypes.values());
    }

}
