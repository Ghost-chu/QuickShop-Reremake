package org.maxgamer.quickshop.Permission;

public enum PermissionProviderType {
    //BUKKIT(0), VAULT(1), LUCKPERMS(2), PERMISSIONEX(3), GROUPMANAGER(4);
    BUKKIT(0);
    int id;

    PermissionProviderType(int id) {
        this.id = id;
    }

    public int toID() {
        return this.id;
    }

    public static PermissionProviderType fromID(int id) throws IllegalArgumentException {
        for (PermissionProviderType child : PermissionProviderType.values()) {
            if (child.toID() == id) {
                return child;
            }
        }
        throw new IllegalArgumentException("Type not exists");
    }
}
