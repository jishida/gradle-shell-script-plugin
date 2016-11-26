package com.github.jishida.gradle.shellscript

import org.gradle.api.Project
import org.gradle.internal.impldep.org.apache.commons.collections.list.UnmodifiableList

import static com.github.jishida.gradle.shellscript.ShellScriptStrings.DEFAULT_UNIX_SHELL
import static com.github.jishida.gradle.shellscript.util.EnvironmentUtils.windows

class ShellScriptInfo {
    final Project project

    final String unixShell
    final List<String> shellArgs
    final Msys2Info msys2

    ShellScriptInfo(final ShellScriptExtension extension) {
        project = extension.project

        unixShell = extension.unixShell ?: DEFAULT_UNIX_SHELL
        shellArgs = UnmodifiableList.decorate(extension.shellArgs.toList())
        msys2 = new Msys2Info(extension.msys2)
    }

    Object getShellExecutable() {
        windows ? msys2.cache.bashFile : unixShell
    }
}