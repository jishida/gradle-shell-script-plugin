package com.github.jishida.gradle.shellscript.tasks;

import com.github.jishida.gradle.commons.util.Checker;
import com.github.jishida.gradle.shellscript.ShellScriptInfo;
import org.gradle.api.Action;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;
import org.gradle.process.ExecSpec;
import org.gradle.process.internal.streams.SafeStreams;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.github.jishida.gradle.commons.util.EnvironmentUtils.isWindows;
import static com.github.jishida.gradle.shellscript.ShellScriptStrings.DEFAULT_SHELL_SCRIPT_M_SYSTEM;

public class ShellScript extends AbstractShellScriptTask {
    private File scriptFile;
    private String scriptText;
    private File workingDir;
    private String mSystem = DEFAULT_SHELL_SCRIPT_M_SYSTEM;
    private final List<String> args = new ArrayList<String>();
    private final List<String> shellArgs = new ArrayList<String>();
    private ExecResult execResult;
    private OutputStream standardOutput = SafeStreams.systemOut();
    private OutputStream errorOutput = SafeStreams.systemErr();
    private InputStream standardInput = SafeStreams.emptyInput();

    public ShellScript() {
        super();
        workingDir = getProject().getProjectDir();
        if (isWindows()) {
            dependsOn(Msys2Setup.TASK_NAME);
        }
    }

    public File getScriptFile() {
        return scriptFile;
    }

    public void setScriptFile(final File value) {
        scriptFile = value;
    }

    public String getScriptText() {
        return scriptText;
    }

    public void setScriptText(final String value) {
        scriptText = value;
    }

    @Input
    public File getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(final File value) {
        Checker.checkNull(value, "value");
        workingDir = value;
    }

    @Input
    public String getMSystem() {
        return mSystem;
    }

    public void setMSystem(final String value) {
        Checker.checkNull(value, "value");
        mSystem = value;
    }

    public Collection<String> getArgs() {
        return args;
    }

    public void setArgs(final Collection<String> value) {
        args.clear();
        args.addAll(value);
    }

    public void args(final Iterable<String> value) {
        for (String item : value) {
            args.add(item);
        }
    }

    public void args(final String... value) {
        Collections.addAll(args, value);
    }

    public Collection<String> getShellArgs() {
        return shellArgs;
    }

    public void setShellArgs(final Collection<String> value) {
        shellArgs.clear();
        shellArgs.addAll(value);
    }

    public void shellArgs(final Iterable<String> value) {
        for (String item : value) {
            shellArgs.add(item);
        }
    }

    public void shellArgs(final String... value) {
        Collections.addAll(shellArgs, value);
    }

    public ExecResult getExecResult() {
        return execResult;
    }

    public OutputStream getStandardOutput() {
        return standardOutput;
    }

    public void setStandardOutput(final OutputStream value) {
        standardOutput = value;
    }

    public OutputStream getErrorOutput() {
        return errorOutput;
    }

    public void setErrorOutput(final OutputStream value) {
        errorOutput = value;
    }

    public InputStream getStandardInput() {
        return standardInput;
    }

    public void setStandardInput(final InputStream value) {
        standardInput = value;
    }

    @Input
    long getShellScriptFileModified() {
        return scriptFile != null && scriptFile.isFile() ? scriptFile.lastModified() : 0L;
    }

    @Input
    private List<String> getAllArgs() {
        final ShellScriptInfo info = getShellScriptInfo();
        if (info == null) {
            return null;
        }
        final List<String> result = new ArrayList<String>();
        if (isWindows()) {
            result.add("--login");
        }
        if (!info.getShellArgs().isEmpty()) {
            result.addAll(info.getShellArgs());
        }
        if (!shellArgs.isEmpty()) {
            result.addAll(shellArgs);
        }
        if (scriptFile != null) {
            try {
                result.add(scriptFile.getCanonicalPath());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        } else if (scriptText != null) {
            result.add("-c");
            result.add(scriptText);
        } else {
            throw new IllegalStateException("must set `scriptFile` or `scriptText` property.");
        }
        if (!args.isEmpty()) {
            result.addAll(args);
        }
        return result;
    }

    @TaskAction
    protected void run() {
        final ShellScriptInfo info = getShellScriptInfo();

        execResult = getProject().exec(new Action<ExecSpec>() {
            @Override
            public void execute(final ExecSpec it) {
                it.setExecutable(info.getShellExecutable());

                if (isWindows()) {
                    it.environment("MSYSTEM", mSystem);
                    it.environment("CHERE_INVOKING", "1");
                }

                it.args(getAllArgs());

                if (!workingDir.isDirectory()) {
                    workingDir.mkdirs();
                }

                it.setWorkingDir(workingDir);

                it.setStandardInput(standardInput);
                it.setStandardOutput(standardOutput);
                it.setErrorOutput(errorOutput);
            }
        });
    }
}
