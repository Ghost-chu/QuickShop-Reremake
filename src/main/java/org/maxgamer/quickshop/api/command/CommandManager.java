package org.maxgamer.quickshop.api.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The manager that managing all sub-commands that registered
 * Also performing permission checks in there.
 */
public interface CommandManager {
    /**
     * This is a interface to allow addons to register the subcommand into quickshop command manager.
     *
     * @param container The command container to register
     * @throws IllegalStateException Will throw the error if register conflict.
     */
    void registerCmd(@NotNull CommandContainer container);

    /**
     * This is a interface to allow addons to unregister the registered/butil-in subcommand from command manager.
     *
     * @param container The command container to unregister
     */
    void unregisterCmd(@NotNull CommandContainer container);

    /**
     * Gets a list contains all registered commands
     *
     * @return All registered commands.
     */
    @NotNull List<CommandContainer> getRegisteredCommands();

    boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String commandLabel,
            @NotNull String[] cmdArg);

    /**
     * Method for capturing generic type
     */
    @SuppressWarnings("unchecked")
    default <T1, T2 extends T1> T2 capture(T1 type) {
        return (T2) type;
    }

    boolean checkPermissions(CommandSender sender, String commandLabel, String[] cmdArg, List<String> permissionList, PermissionType permissionType, Action action);


    boolean isAdapt(CommandContainer container, CommandSender sender);

    @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String commandLabel,
            @NotNull String[] cmdArg);

    enum Action {
        EXECUTE("execute"),
        TAB_COMPLETE("tab-complete");
        final String name;

        Action(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    enum PermissionType {
        REQUIRE,
        SELECTIVE;
    }
}
