package com.github.jishida.gradle.shellscript

import com.github.jishida.gradle.shellscript.task.Msys2Download
import com.github.jishida.gradle.shellscript.task.Msys2Setup
import com.github.jishida.gradle.shellscript.task.ShellScript
import org.gradle.api.Plugin
import org.gradle.api.Project

import static com.github.jishida.gradle.shellscript.ShellScriptStrings.EXTENSION_NAME
import static com.github.jishida.gradle.shellscript.ShellScriptStrings.Tasks
import static com.github.jishida.gradle.shellscript.util.EnvironmentUtils.windows

class ShellScriptPlugin implements Plugin<Project> {
    @Override
    void apply(final Project project) {
        final extension = project.extensions.create(EXTENSION_NAME, ShellScriptExtension, project)
        if (windows) {
            project.tasks.create(Tasks.MSYS2_DOWNLOAD, Msys2Download)
            project.tasks.create(Tasks.MSYS2_SETUP, Msys2Setup)
        }
        project.extensions.extraProperties.set(ShellScript.simpleName, ShellScript)

        project.afterEvaluate {
            extension.configure()
        }
    }
}