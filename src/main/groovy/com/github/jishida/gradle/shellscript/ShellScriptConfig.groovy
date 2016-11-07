package com.github.jishida.gradle.shellscript

import org.gradle.api.Project
import org.gradle.internal.impldep.org.apache.commons.collections.list.UnmodifiableList

import static com.github.jishida.gradle.shellscript.ShellScriptStrings.DEFAULT_UNIX_SHELL

class ShellScriptConfig {
    final Project project

    final String unixShell
    final List<String> shellArgs
    final Msys2Config msys2

    ShellScriptConfig(final ShellScriptExtension extension) {
        project = extension.project

        unixShell = extension.unixShell ?: DEFAULT_UNIX_SHELL
        shellArgs = UnmodifiableList.decorate(extension.shellArgs?.collect { it.toString() } ?: [])
        msys2 = new Msys2Config(extension.msys2)
    }
}