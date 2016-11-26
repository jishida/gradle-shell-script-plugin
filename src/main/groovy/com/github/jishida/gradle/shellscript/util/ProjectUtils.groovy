package com.github.jishida.gradle.shellscript.util

import com.github.jishida.gradle.shellscript.Msys2CacheInfo
import com.github.jishida.gradle.shellscript.Msys2Info
import com.github.jishida.gradle.shellscript.Msys2Spec
import com.github.jishida.gradle.shellscript.ShellScriptExtension
import com.github.jishida.gradle.shellscript.ShellScriptPlugin
import org.gradle.api.Project

final class ProjectUtils {
    static ShellScriptExtension getShellScriptExtension(final Project project) {
        project.extensions.findByType(ShellScriptExtension)
    }

    static Msys2Spec getMsys2Extension(final Project project) {
        getShellScriptExtension(project)?.msys2
    }

    static Msys2CacheInfo getCacheInfo(final Project project) {
        final cacheProject = getMsys2Extension(project).cacheProject ?: project.rootProject
        if (getMsys2Extension(cacheProject) == null) {
            cacheProject.apply plugin: ShellScriptPlugin
            getShellScriptExtension(cacheProject).msys2.cacheProject = cacheProject
        }
        cacheProject == project ? null : getShellScriptExtension(cacheProject).configure().msys2.cache
    }
}