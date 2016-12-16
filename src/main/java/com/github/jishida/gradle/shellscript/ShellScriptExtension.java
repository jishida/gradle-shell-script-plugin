package com.github.jishida.gradle.shellscript;

import com.github.jishida.gradle.commons.util.Checker;
import com.github.jishida.gradle.shellscript.tasks.Msys2Setup;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.ClosureBackedAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.github.jishida.gradle.commons.util.EnvironmentUtils.isWindows;

public class ShellScriptExtension {
    private final Project project;
    private ShellScriptInfo info;

    private final Msys2Spec msys2;
    private Object unixShell = ShellScriptStrings.DEFAULT_UNIX_SHELL;
    private final List<String> shellArgs = new ArrayList<String>();

    public ShellScriptExtension(final Project project) {
        this.project = project;
        msys2 = new Msys2Spec(project);
    }

    public Project getProject() {
        return project;
    }

    public ShellScriptInfo getInfo() {
        return info;
    }

    public Msys2Spec getMsys2() {
        return msys2;
    }

    public void msys2(final Action<Msys2Spec> action) {
        Checker.checkNull(action, "action");
        action.execute(msys2);
    }

    public void msys2(final Closure closure) {
        Checker.checkNull(closure, "closure");
        msys2(new ClosureBackedAction<Msys2Spec>(closure));
    }

    public Object getUnixShell() {
        return unixShell;
    }

    public void setUnixShell(final Object value) {
        unixShell = value;
    }

    public Collection<String> getShellArgs() {
        return shellArgs;
    }

    public void setShellArgs(final Collection<String> value) {
        Checker.checkNull(value, "value");
        shellArgs.clear();
        shellArgs.addAll(value);
    }

    public void shellArgs(final Iterable<String> value) {
        Checker.checkNull(value, "value");
        for (String item : value) {
            shellArgs.add(item);
        }
    }

    public void shellArgs(final String... value) {
        Collections.addAll(shellArgs, value);
    }

    ShellScriptInfo configure() {
        if (info == null) {
            info = new ShellScriptInfo(this);
            if (isWindows()) {
                final Task msys2Setup = project.getTasks().getByName(Msys2Setup.TASK_NAME);
                if (!info.getMsys2().hasCache()) {
                    msys2Setup.dependsOn(info.getMsys2().getCache().getProject().getTasks().getByName(Msys2Setup.TASK_NAME));
                }
            }
        }
        return info;
    }
}
