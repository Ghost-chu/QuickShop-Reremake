package org.maxgamer.quickshop.database;

import lombok.Getter;

public enum DataTypeMapping {
    TINYINT("tinyint","integer"),
    SMALLINT("smallint","integer"),
    MEDIUMINT("mediumint","integer"),
    INT("int","integer"),
    BIGINT("bigint","integer"),
    FLOAT("float","float"),
    DOUBLE("double","float"),
    VARCHAR("varchar","varchar"),
    TINYBLOB("tinyblob","blob"),
    TINYTEXT("tinytext","tinytext"),
    BLOB("blob","blob"),
    TEXT("text","text"),
    MEDIUMBLOB("mediumblob","blob"),
    MEDIUMTEXT("mediumtext","text"),
    LONGBLOB("longblob","blob"),
    LONGTEXT("longtext","text");



    @Getter
    private final String mysql;
    @Getter
    private final String sqlite;
    DataTypeMapping( String mysql, String sqlite ){
        this.mysql = mysql;
        this.sqlite = sqlite;
    }
}
