package com.github.jishida.gradle.shellscript

import com.github.jishida.gradle.shellscript.archive.Unarchiver
import groovy.transform.PackageScope
import org.gradle.api.Project
import org.gradle.api.file.FileTree

import static com.github.jishida.gradle.shellscript.util.ProjectUtils.getCacheInfo

class Msys2Info {
    private final _hasCache

    final Project project
    final Msys2CacheInfo cache
    final boolean setup

    @PackageScope
    Msys2Info(final Msys2Spec spec) {
        project = spec.project

        final cacheInfo = getCacheInfo(project)
        cache = cacheInfo ?: new Msys2CacheInfo(spec)
        _hasCache = cacheInfo == null
        setup = _hasCache ? spec.setup : false
    }

    boolean hasCache() {
        _hasCache
    }
}