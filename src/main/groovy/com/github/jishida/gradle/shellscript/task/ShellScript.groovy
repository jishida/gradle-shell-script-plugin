package com.github.jishida.gradle.shellscript.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import static com.github.jishida.gradle.shellscript.ShellScriptStrings.*
import static com.github.jishida.gradle.shellscript.util.EnvironmentUtils.windows

class ShellScript extends DefaultTask implements ShellScriptTask {
    File workingDir
    List<String> args
    String mSystem = DEFAULT_SHELL_SCRIPT_M_SYSTEM
    File scriptFile

    ShellScript() {
        super()
        group = ID
        if (windows) {
            dependsOn(Tasks.MSYS2_SETUP)
        }
    }

    @Input
    Map getShellScriptInputs() {
        final def result = [:]
        result['mSystem'] = mSystem ?: (Object) DEFAULT_SHELL_SCRIPT_M_SYSTEM

        if (workingDir != null) {
            result['workingDir'] = workingDir
        }
        if (args != null) {
            result['args'] = args
        }
        if (scriptFile != null) {
            result['scriptFile'] = scriptFile
            if (scriptFile.file) {
                result['scriptFileModified'] = scriptFile.lastModified()
            }
        }
        result
    }

    @Input
    String getShellExecutable() {
        windows ? shellScript?.msys2?.bashFile?.canonicalPath : shellScript?.unixShell
    }

    @Input
    List<String> getShellArgs() {
        shellScript?.shellArgs
    }

    @TaskAction
    protected void run() {
        final def workingDir_ = workingDir ?: project.projectDir

        project.exec {
            it.executable = shellExecutable

            final def args_ = []
            if (windows) {
                args_ << '--login'

                it.environment['MSYSTEM'] = mSystem ?: DEFAULT_SHELL_SCRIPT_M_SYSTEM
                it.environment['CHERE_INVOKING'] = '1'
            }
            if (!shellArgs.empty)
                args_.addAll(shellArgs)
            if (args != null && !args.empty)
                args_.addAll(args)
            if (scriptFile != null)
                args_ << scriptFile.canonicalPath
            it.args = args_

            if (workingDir_ != null) {
                workingDir_.mkdirs()
            }
            it.workingDir = workingDir_
        }.rethrowFailure()
    }
}