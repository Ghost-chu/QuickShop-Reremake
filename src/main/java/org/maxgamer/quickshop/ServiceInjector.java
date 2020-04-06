package org.maxgamer.quickshop;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.database.DatabaseCore;
import org.maxgamer.quickshop.economy.EconomyCore;
import org.maxgamer.quickshop.util.Language.Game.GameLanguage;
import org.maxgamer.quickshop.util.matcher.item.ItemMatcher;

public class ServiceInjector {
    public static EconomyCore getEconomyCore(@NotNull EconomyCore def){
        @Nullable RegisteredServiceProvider<? extends EconomyCore> registeredServiceProvider =
                Bukkit.getServicesManager().getRegistration(EconomyCore.class);
        if(registeredServiceProvider == null){
            return def;
        }else{
            return registeredServiceProvider.getProvider();
        }
    }
    public static ItemMatcher getItemMatcher(@NotNull ItemMatcher def){
        @Nullable RegisteredServiceProvider<? extends ItemMatcher> registeredServiceProvider =
                Bukkit.getServicesManager().getRegistration(ItemMatcher.class);
        if(registeredServiceProvider == null){
            return def;
        }else{
            return registeredServiceProvider.getProvider();
        }
    }
    public static GameLanguage getGameLanguage(@NotNull GameLanguage def){
        @Nullable RegisteredServiceProvider<? extends GameLanguage> registeredServiceProvider =
                Bukkit.getServicesManager().getRegistration(GameLanguage.class);
        if(registeredServiceProvider == null){
            return def;
        }else{
            return registeredServiceProvider.getProvider();
        }
    }
    public static DatabaseCore getDatabaseCore(@NotNull DatabaseCore def){
        @Nullable RegisteredServiceProvider<? extends DatabaseCore> registeredServiceProvider =
                Bukkit.getServicesManager().getRegistration(DatabaseCore.class);
        if(registeredServiceProvider == null){
            return def;
        }else{
            return registeredServiceProvider.getProvider();
        }
    }
}
