package com.github.jishida.gradle.shellscript

import groovy.transform.PackageScope
import org.gradle.api.Project

import static com.github.jishida.gradle.shellscript.ShellScriptStrings.DEFAULT_UNIX_SHELL

class ShellScriptExtension {
    private ShellScriptConfig config

    final Project project
    final Msys2Extension msys2

    def unixShell = DEFAULT_UNIX_SHELL
    def shellArgs = []

    ShellScriptExtension(final Project project) {
        this.project = project
        msys2 = new Msys2Extension(project)
    }

    ShellScriptConfig getConfig() { config }

    @PackageScope
    ShellScriptConfig configure() {
        config ?: (config = new ShellScriptConfig(this))
    }

    final void msys2(Closure closure) {
        closure.delegate = msys2
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call()
    }
}