package com.gigapress.dynamicupdate.domain;

public enum DependencyStrength {
    STRONG,  // Breaking changes will affect dependent
    WEAK,    // Changes might affect dependent
    OPTIONAL // Changes unlikely to affect dependent
}
