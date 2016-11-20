package com.github.jishida.gradle.shellscript.task

import com.github.jishida.gradle.shellscript.ShellScriptConfig
import org.gradle.api.DefaultTask

import static com.github.jishida.gradle.shellscript.ShellScriptStrings.PLUGIN_ID
import static com.github.jishida.gradle.shellscript.util.ProjectUtils.getShellScriptExtension

class AbstractShellScriptTask extends DefaultTask implements ShellScriptTask {
    private ShellScriptConfig _shellScriptConfig

    AbstractShellScriptTask() {
        group = PLUGIN_ID
    }

    @Override
    ShellScriptConfig getShellScriptConfig() {
        _shellScriptConfig ?: (_shellScriptConfig = getShellScriptExtension(project)?.config)
    }
}