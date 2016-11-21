package com.github.jishida.gradle.shellscript.task

import com.github.jishida.gradle.shellscript.ShellScriptConfig
import org.gradle.api.Task

import static com.github.jishida.gradle.shellscript.util.ProjectUtils.getShellScriptExtension

trait ShellScriptTask implements Task {
    ShellScriptConfig getShellScriptConfig() {
        getShellScriptExtension(project).config
    }
}