package org.maxgamer.quickshop.Util;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
public class IncompatibleChecker {
    private Set<String> incompatibleVersionList;

    public IncompatibleChecker() {
        incompatibleVersionList = new HashSet<>();
        mc1_5();
        mc1_6();
        mc1_7();
        mc1_8();
        mc1_9();
        mc1_10();
        mc1_11();
        mc1_12();
        mc1_13();
    }

    public boolean isIncompatible(String version) {
        return incompatibleVersionList.contains(version);
    }

    private void mc1_10() {
        incompatibleVersionList.add("v1_10_R1");
    }

    private void mc1_11() {
        incompatibleVersionList.add("v1_11_R1");
    }

    private void mc1_12() {
        incompatibleVersionList.add("v1_12_R1");
    }

    private void mc1_13() {
        incompatibleVersionList.add("v1_13_R1"); //1.13.0-1.13.1 no Tag feature.
    }

    private void mc1_5() {
        incompatibleVersionList.add("v1_5_R1");
        incompatibleVersionList.add("v1_5_R2");
        incompatibleVersionList.add("v1_5_R3");
    }

    private void mc1_6() {
        incompatibleVersionList.add("v1_6_R1");
        incompatibleVersionList.add("v1_6_R2");
        incompatibleVersionList.add("v1_6_R3");
    }

    private void mc1_7() {
        incompatibleVersionList.add("v1_7_R1");
        incompatibleVersionList.add("v1_7_R2");
        incompatibleVersionList.add("v1_7_R3");
        incompatibleVersionList.add("v1_7_R4");
    }

    private void mc1_8() {
        incompatibleVersionList.add("v1_8_R1");
        incompatibleVersionList.add("v1_8_R2");
        incompatibleVersionList.add("v1_8_R3");
    }

    private void mc1_9() {
        incompatibleVersionList.add("v1_9_R1");
        incompatibleVersionList.add("v1_9_R2");
    }
}
