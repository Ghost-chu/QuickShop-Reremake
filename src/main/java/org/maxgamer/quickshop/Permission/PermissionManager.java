package org.maxgamer.quickshop.Permission;

import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;
import sun.awt.geom.AreaOp;

@Getter
public class PermissionManager {
    private QuickShop plugin;
    private PermissionProvider provider;
    private PermissionProviderType providerType;
    /**
     * The manager to call permission providers
     * @param plugin Instance
     */
    public PermissionManager(QuickShop plugin){
        this.plugin=plugin;
        try {
            this.providerType = PermissionProviderType.fromID(plugin.getConfig().getInt("permission-type"));
        }catch (IllegalArgumentException e){
            plugin.getLogger().warning("Falling back to Bukkit permission provider...");
            this.providerType = PermissionProviderType.BUKKIT;
        }
        try {
            switch (providerType) {
                case BUKKIT:
                    //noinspection DuplicateBranchesInSwitch
                    provider = new BukkitPermsProvider();
                    break;
                case VAULT:
                    provider = new VaultPermissionProvider();
                    break;
                case LUCKPERMS:
                    provider = new LuckPermsProvider(this);
                    break;
                case PERMISSIONEX:
                    provider = new PexPermissionProvider();
                    break;
                case GROUPMANAGER:
                    provider = new GroupManagerProvider();
                    break;
                default:
                    provider = new BukkitPermsProvider();
                    break;
            }
        }catch (ProviderIsEmptyException empty){
            plugin.getLogger().warning("Provider "+providerType.name()+" doesn't work, falling back to BUKKIT.");
            provider = new BukkitPermsProvider();
        }catch (Throwable th){
            th.printStackTrace();
            plugin.getLogger().warning("Provider "+providerType.name()+" failed loading, falling back to BUKKIT.");
            provider = new BukkitPermsProvider();
        }
        plugin.getLogger().info("Selected permission provider: "+provider.getName());
    }

    /**
     * Check the permission for sender
     * @param sender The CommandSender you want check
     * @param permission The permission node wait to check
     * @return The result of check
     */
    public boolean hasPermission(@NotNull CommandSender sender, @NotNull String permission){
        try{
            boolean result = provider.hasPermission(sender, permission);
            if(Util.isDevMode()){
                try{
                    PermissionInfomationContainer container = provider.getDebugInfo(sender, permission);
                    Util.debugLog("=======");
                    Util.debugLog("Result: "+result);
                    Util.debugLog("Sender: "+container.getSender().getName());
                    Util.debugLog("Permission Node: "+container.getPermission());
                    Util.debugLog("Primary Group: "+container.getGroupName());
                    Util.debugLog("Other infos: "+container.getOtherInfos());
                }catch (Throwable th){
                    th.printStackTrace();
                    Util.debugLog("Exception throwed when getting debug messages.");
                }
            }
            return result;
        }catch (Throwable th){
            plugin.getSentryErrorReporter().ignoreThrow();
            th.printStackTrace();
            plugin.getLogger().info("A error happend, if you believe this is QuickShop problem, please report to us on Issue Tracker or Discord.");
            return false;
        }
    }
}
