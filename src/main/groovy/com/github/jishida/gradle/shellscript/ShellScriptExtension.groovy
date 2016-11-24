package com.github.jishida.gradle.shellscript

import groovy.transform.PackageScope
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.internal.ClosureBackedAction

import static com.github.jishida.gradle.shellscript.ShellScriptStrings.DEFAULT_UNIX_SHELL
import static com.github.jishida.gradle.shellscript.ShellScriptStrings.Tasks.MSYS2_DOWNLOAD
import static com.github.jishida.gradle.shellscript.ShellScriptStrings.Tasks.MSYS2_SETUP
import static com.github.jishida.gradle.shellscript.util.EnvironmentUtils.windows

class ShellScriptExtension {
    private ShellScriptInfo _info
    private final List<String> _shellArgs = []

    final Project project
    final Msys2Spec msys2

    Object unixShell = DEFAULT_UNIX_SHELL

    ShellScriptExtension(final Project project) {
        this.project = project
        msys2 = new Msys2Spec(project)
    }

    ShellScriptInfo getInfo() { _info }

    @PackageScope
    ShellScriptInfo configure() {
        if (_info == null) {
            _info = new ShellScriptInfo(this)
            if (windows) {
                final msys2Setup = project.tasks.getByName(MSYS2_SETUP)
                if (_info.msys2.hasCache()) {
                    msys2Setup.dependsOn project.tasks.getByName(MSYS2_DOWNLOAD)
                } else {
                    msys2Setup.dependsOn _info.msys2.cache.project.tasks.getByName(MSYS2_SETUP)
                }
            }
        }
        _info
    }

    void msys2(final Action<Msys2Spec> action) {
        action.execute(msys2)
    }

    void msys2(final Closure closure) {
        msys2(new ClosureBackedAction<Msys2Spec>(closure))
    }

    Collection<String> getShellArgs() {
        _shellArgs
    }

    void setShellArgs(final Collection<String> value) {
        _shellArgs.clear()
        _shellArgs.addAll(value)
    }

    void shellArgs(final Iterable<String> value) {
        _shellArgs.addAll(value)
    }

    void shellArgs(final String... value) {
        _shellArgs.addAll(value)
    }
}