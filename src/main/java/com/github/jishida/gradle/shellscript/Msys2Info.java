package com.github.jishida.gradle.shellscript;

import org.gradle.api.Project;

public class Msys2Info {
    private final Project project;
    private final Msys2CacheInfo cache;
    private final boolean setup;
    private final boolean hasCache;

    Msys2Info(final Msys2Spec spec) {
        project = spec.getProject();

        final Msys2CacheInfo cacheInfo = ShellScriptUtils.getCacheInfo(project);
        cache = cacheInfo == null ? new Msys2CacheInfo(spec) : cacheInfo;
        hasCache = cacheInfo == null;
        setup = hasCache && spec.isSetup();
    }

    public Project getProject() {
        return project;
    }

    public Msys2CacheInfo getCache() {
        return cache;
    }

    public boolean isSetup() {
        return setup;
    }

    public boolean hasCache() {
        return hasCache;
    }
}
