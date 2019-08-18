package org.maxgamer.quickshop.Event;

public enum ProtectionCheckStatus {
    BEGIN(0), END(1);
    int statusCode;

    ProtectionCheckStatus(int statusCode) {
        this.statusCode = statusCode;
    }
}
