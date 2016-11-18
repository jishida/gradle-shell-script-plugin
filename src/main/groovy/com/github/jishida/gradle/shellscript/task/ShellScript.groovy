package com.github.jishida.gradle.shellscript.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import static com.github.jishida.gradle.shellscript.ShellScriptStrings.*
import static com.github.jishida.gradle.shellscript.util.EnvironmentUtils.windows

class ShellScript extends DefaultTask implements ShellScriptTask {
    File scriptFile
    String scriptText
    File workingDir
    List<String> args
    List<String> shellArgs
    String mSystem = DEFAULT_SHELL_SCRIPT_M_SYSTEM

    ShellScript() {
        super()
        group = ID
        if (windows) {
            dependsOn(Tasks.MSYS2_SETUP)
        }
    }

    @Input
    Map getShellScriptInputs() {
        final def result = [
                mSystem: mSystem ?: DEFAULT_SHELL_SCRIPT_M_SYSTEM
        ]
        if (workingDir != null) {
            result['workingDir'] = workingDir
        }
        if (scriptFile != null && scriptFile.file) {
            result['scriptFileModified'] = scriptFile.lastModified()
        }
        result
    }

    @Input
    String getShellExecutable() {
        windows ? shellScript?.msys2?.bashFile?.canonicalPath : shellScript?.unixShell
    }

    @Input
    List<String> getAllArgs() {
        final def config = shellScript
        if (config == null) return null
        final def result = []
        if (windows) {
            result << '--login'
        }
        if (config.shellArgs != null && !config.shellArgs.empty) {
            result.addAll(config.shellArgs)
        }
        if (shellArgs != null && !shellArgs.empty) {
            result.addAll(shellArgs)
        }
        if (scriptFile != null) {
            result << scriptFile.canonicalPath
        } else if (scriptText != null) {
            result.addAll(['-c', scriptText])
        } else {
            throw new IllegalStateException('must set `scriptFile` or `scriptText` property.')
        }
        if (args != null && !args.empty) {
            result.addAll(args)
        }
        result
    }

    @TaskAction
    protected void run() {
        final def workingDir_ = workingDir ?: project.projectDir

        project.exec {
            it.executable = shellExecutable

            if (windows) {
                it.environment['MSYSTEM'] = mSystem ?: DEFAULT_SHELL_SCRIPT_M_SYSTEM
                it.environment['CHERE_INVOKING'] = '1'
            }

            it.args = allArgs

            if (workingDir_ != null) {
                workingDir_.mkdirs()
            }
            it.workingDir = workingDir_
        }.rethrowFailure()
    }
}