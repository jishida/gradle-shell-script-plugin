package com.github.jishida.gradle.shellscript

import groovy.transform.PackageScope
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.internal.ClosureBackedAction

import static com.github.jishida.gradle.shellscript.ShellScriptStrings.DEFAULT_UNIX_SHELL
import static com.github.jishida.gradle.shellscript.ShellScriptStrings.Tasks.MSYS2_SETUP
import static com.github.jishida.gradle.shellscript.util.EnvironmentUtils.windows

class ShellScriptExtension {
    private ShellScriptConfig _config
    private final List<String> _shellArgs = []

    final Project project
    final Msys2Spec msys2

    Object unixShell = DEFAULT_UNIX_SHELL

    ShellScriptExtension(final Project project) {
        this.project = project
        msys2 = new Msys2Spec(project)
    }

    ShellScriptConfig getConfig() { _config }

    @PackageScope
    ShellScriptConfig configure() {
        if (_config == null) {
            _config = new ShellScriptConfig(this)
            if (windows && _config.msys2.cacheProject != null) {
                final msys2Setup = project.tasks.getByName(MSYS2_SETUP)
                msys2Setup.dependsOn(_config.msys2.cacheProject.tasks.getByName(MSYS2_SETUP))
            }
        }
        _config
    }

    void msys2(final Action<Msys2Spec> action) {
        action.execute(msys2)
    }

    void msys2(final Closure closure) {
        msys2(new ClosureBackedAction<Msys2Spec>(closure))
    }

    Iterable<String> getShellArgs() {
        _shellArgs
    }

    void setShellArgs(final Iterable<String> value) {
        _shellArgs.clear()
        for (arg in value) {
            _shellArgs.add(arg)
        }
    }

    void shellArgs(final Iterable<String> value) {
        for (arg in value) {
            _shellArgs.add(arg)
        }
    }

    void shellArgs(final String... value) {
        for (arg in value) {
            _shellArgs.add(arg)
        }
    }
}