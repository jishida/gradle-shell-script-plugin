package com.github.jishida.gradle.shellscript.task

import com.github.jishida.gradle.shellscript.ShellScriptInfo
import org.gradle.api.DefaultTask

import static com.github.jishida.gradle.shellscript.ShellScriptStrings.PLUGIN_ID
import static com.github.jishida.gradle.shellscript.util.ProjectUtils.getShellScriptExtension

class AbstractShellScriptTask extends DefaultTask implements ShellScriptTask {
    private ShellScriptInfo _shellScriptInfo

    AbstractShellScriptTask() {
        group = PLUGIN_ID
    }

    @Override
    ShellScriptInfo getShellScriptInfo() {
        _shellScriptInfo ?: (_shellScriptInfo = getShellScriptExtension(project)?.info)
    }
}