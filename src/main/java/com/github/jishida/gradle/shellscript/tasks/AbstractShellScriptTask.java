package com.github.jishida.gradle.shellscript.tasks;

import com.github.jishida.gradle.shellscript.Msys2Info;
import com.github.jishida.gradle.shellscript.ShellScriptExtension;
import com.github.jishida.gradle.shellscript.ShellScriptInfo;
import org.gradle.api.internal.AbstractTask;

import static com.github.jishida.gradle.shellscript.ShellScriptUtils.getShellScriptExtension;
import static com.github.jishida.gradle.shellscript.ShellScriptStrings.PLUGIN_ID;

public abstract class AbstractShellScriptTask extends AbstractTask {
    private ShellScriptInfo shellScriptInfo;

    AbstractShellScriptTask() {
        super();
        setGroup(PLUGIN_ID);
    }

    public ShellScriptInfo getShellScriptInfo() {
        if (shellScriptInfo == null) {
            final ShellScriptExtension extension = getShellScriptExtension(getProject());
            if (extension == null || extension.getInfo() == null) return null;
            shellScriptInfo = extension.getInfo();
        }
        return shellScriptInfo;
    }

    public Msys2Info getMsys2Info() {
        final ShellScriptInfo info = getShellScriptInfo();
        return info == null ? null : info.getMsys2();
    }
}
