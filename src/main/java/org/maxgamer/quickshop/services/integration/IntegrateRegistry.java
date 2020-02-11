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

package org.maxgamer.quickshop.services.integration;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
@AllArgsConstructor
public class IntegrateRegistry {
    @NotNull private final Map<Plugin, List<RegisteredIntegration>> registry = new HashMap<>();
    @NotNull private List<Plugin> blacklist;
    public void register(@NotNull Plugin plugin, @NotNull Callable<IntegratedPlugin> callback) throws IllegalStateException{
        if(blacklist.contains(plugin)){
            throw new IllegalStateException("Cannot register this integration because user blacklisted this integration.");
        }
        List<RegisteredIntegration> registeredIntegrations = this.registry.getOrDefault(plugin, Lists.newArrayList());
        registeredIntegrations.add(new RegisteredIntegration(plugin,callback));
        this.registry.put(plugin,registeredIntegrations);
    }
    public void unregister(@NotNull Plugin plugin, @NotNull Callable<IntegratedPlugin> callback){
        List<RegisteredIntegration> registeredIntegrations = this.registry.getOrDefault(plugin, Lists.newArrayList());
        registeredIntegrations.remove(new RegisteredIntegration(plugin,callback));
        if(registeredIntegrations.isEmpty()){
            this.registry.remove(plugin);
        }else{
            this.registry.put(plugin,registeredIntegrations);
        }

    }
    public void unregisterAll(@NotNull Plugin plugin){
        this.registry.remove(plugin);
    }
    @NotNull
    public List<Plugin> getRegisteredPlugins(){
        return new ArrayList<>(registry.keySet());
    }
    @NotNull
    public List<RegisteredIntegration> getRegisteredListeners(@NotNull Plugin plugin){
        return registry.getOrDefault(plugin,Lists.newArrayList());
    }

}
@AllArgsConstructor
class RegisteredIntegration{
    private @NotNull Plugin plugin;
    private @NotNull Callable<IntegratedPlugin> callback;
}