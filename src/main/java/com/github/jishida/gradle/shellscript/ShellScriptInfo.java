package com.github.jishida.gradle.shellscript;

import org.gradle.api.Project;

import java.util.List;

import static com.github.jishida.gradle.commons.util.CollectionUtils.enumerable;
import static com.github.jishida.gradle.commons.util.EnvironmentUtils.isWindows;
import static com.github.jishida.gradle.shellscript.ShellScriptStrings.DEFAULT_UNIX_SHELL;

public class ShellScriptInfo {
    private final Project project;
    private final Object unixShell;
    private final List<String> shellArgs;
    private final Msys2Info msys2;

    ShellScriptInfo(final ShellScriptExtension extension) {
        project = extension.getProject();
        unixShell = extension.getUnixShell() == null ? DEFAULT_UNIX_SHELL : extension.getUnixShell();
        shellArgs = enumerable(extension.getShellArgs()).toImmutableList();
        msys2 = new Msys2Info(extension.getMsys2());
    }

    public Project getProject() {
        return project;
    }

    public Object getUnixShell() {
        return unixShell;
    }

    public List<String> getShellArgs() {
        return shellArgs;
    }

    public Msys2Info getMsys2() {
        return msys2;
    }

    public Object getShellExecutable() {
        return isWindows() ? msys2.getCache().getBashFile() : unixShell;
    }
}
