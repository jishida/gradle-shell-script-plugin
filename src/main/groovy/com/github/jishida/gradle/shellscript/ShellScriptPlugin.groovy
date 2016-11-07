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
        final def extension = project.extensions.create(EXTENSION_NAME, ShellScriptExtension, project)
        final def msys2Download = windows ? project.tasks.create(Tasks.MSYS2_DOWNLOAD, Msys2Download) : null
        final def msys2Setup = windows ? project.tasks.create(Tasks.MSYS2_SETUP, Msys2Setup) : null
        project.extensions.extraProperties.set(ShellScript.simpleName, ShellScript)

        project.afterEvaluate {
            final def config = extension.configure()
            if (windows && config.msys2.cacheProject != null) {
                msys2Setup.dependsOn(config.msys2.cacheProject.tasks.getByName(Tasks.MSYS2_SETUP))
            }
        }
    }
}