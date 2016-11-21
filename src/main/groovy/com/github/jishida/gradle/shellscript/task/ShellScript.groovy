package com.github.jishida.gradle.shellscript.task

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import static com.github.jishida.gradle.shellscript.ShellScriptStrings.DEFAULT_SHELL_SCRIPT_M_SYSTEM
import static com.github.jishida.gradle.shellscript.ShellScriptStrings.Tasks
import static com.github.jishida.gradle.shellscript.util.EnvironmentUtils.windows

class ShellScript extends AbstractShellScriptTask {
    private final List<String> _args = []
    private final List<String> _shellArgs = []

    File scriptFile
    String scriptText
    File workingDir
    String mSystem = DEFAULT_SHELL_SCRIPT_M_SYSTEM

    ShellScript() {
        if (windows) {
            dependsOn(Tasks.MSYS2_SETUP)
        }
    }

    Iterable<String> getArgs() {
        _args
    }

    void setArgs(final Iterable<String> value) {
        _args.clear()
        for (arg in value) {
            _args.add(arg)
        }
    }

    void args(final Iterable<String> value) {
        for (arg in value) {
            _args.add(arg)
        }
    }

    void args(final String... value) {
        for (arg in value) {
            _args.add(arg)
        }
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

    @Input
    Map getShellScriptInputs() {
        final result = [:]
        result['mSystem'] = mSystem ?: DEFAULT_SHELL_SCRIPT_M_SYSTEM
        if (workingDir != null)
            result['workingDir'] = workingDir
        if (scriptFile != null && scriptFile.file)
            result['scriptFileModified'] = scriptFile.lastModified()
        result
    }

    @Input
    Object getShellExecutable() {
        shellScriptConfig?.shellExecutable
    }

    @Input
    List<String> getAllArgs() {
        if (shellScriptConfig == null) return null
        final result = []
        if (windows) result.add('--login')
        if (!shellScriptConfig.shellArgs.empty) result.addAll(shellScriptConfig.shellArgs)
        if (!_shellArgs.empty) result.addAll(_shellArgs)
        if (scriptFile != null) {
            result.add(scriptFile.canonicalPath)
        } else if (scriptText != null) {
            result.add('-c')
            result.add(scriptText)
        } else {
            throw new IllegalStateException('must set `scriptFile` or `scriptText` property.')
        }
        if (!_args.empty) result.addAll(_args)
        result
    }

    @TaskAction
    protected void run() {
        final workingDir_ = workingDir ?: project.projectDir

        project.exec {
            it.executable shellExecutable

            if (windows) {
                it.environment['MSYSTEM'] = mSystem ?: DEFAULT_SHELL_SCRIPT_M_SYSTEM
                it.environment['CHERE_INVOKING'] = '1'
            }

            it.args allArgs

            if (workingDir_ != null) {
                workingDir_.mkdirs()
            }
            it.workingDir = workingDir_
        }.rethrowFailure()
    }
}