package org.maxgamer.quickshop.database;

import lombok.Getter;

public enum DataTypeMapping {
    INT("int", "integer"),
    BIGINT("bigint", "integer"),
    FLOAT("float", "float"),
    DOUBLE("double", "float"),
    VARCHAR("varchar", "varchar"),
    TEXT("text", "text"),
    LONGBLOB("longblob", "blob"),
    LONGTEXT("longtext", "text");


    @Getter
    private final String mysql;
    @Getter
    private final String sqlite;

    DataTypeMapping(String mysql, String sqlite) {
        this.mysql = mysql;
        this.sqlite = sqlite;
    }
}
