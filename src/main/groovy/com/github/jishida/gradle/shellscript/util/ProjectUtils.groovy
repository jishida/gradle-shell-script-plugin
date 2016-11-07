package com.github.jishida.gradle.shellscript.util

import com.github.jishida.gradle.shellscript.Msys2Config
import com.github.jishida.gradle.shellscript.Msys2Extension
import com.github.jishida.gradle.shellscript.ShellScriptExtension
import com.github.jishida.gradle.shellscript.ShellScriptPlugin
import org.gradle.api.Project

final class ProjectUtils {
    static ShellScriptExtension getShellScriptExtension(final Project project) {
        project.extensions.findByType(ShellScriptExtension)
    }

    static Msys2Extension getMsys2Extension(final Project project) {
        getShellScriptExtension(project)?.msys2
    }

    static Msys2Config getBaseMsys2Config(final Project project) {
        final def provisional = getMsys2Extension(project).cacheProject ?: project.rootProject
        if (provisional == project.rootProject && getMsys2Extension(provisional) == null) {
            provisional.apply plugin: ShellScriptPlugin
        }
        provisional == project ? null : getShellScriptExtension(provisional)?.configure()?.msys2
    }
}