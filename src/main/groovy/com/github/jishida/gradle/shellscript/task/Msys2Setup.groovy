package com.github.jishida.gradle.shellscript.task

import com.github.jishida.gradle.shellscript.Msys2Config
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*

import static com.github.jishida.gradle.shellscript.ShellScriptStrings.ID
import static com.github.jishida.gradle.shellscript.ShellScriptStrings.Tasks

class Msys2Setup extends DefaultTask implements ShellScriptTask {
    Msys2Setup() {
        super()
        group = ID
        dependsOn(Tasks.MSYS2_DOWNLOAD)
    }

    Msys2Config getConfig() {
        shellScript?.msys2
    }

    @InputFile
    File getArchiveFile() {
        config?.archiveFile
    }

    @OutputDirectory
    File getOutputDir() {
        config?.expandDir
    }

    @Input
    boolean isSetup() {
        config?.setup
    }

    @Input
    Class getUnarchiverClass() {
        config?.unarchiverClass
    }

    @OutputFile
    File getBashFile() {
        config?.bashFile
    }

    @TaskAction
    void setup() {
        final config = shellScript.msys2
        if (!config.setup) return

        config.expandDir.deleteDir()
        config.expandDir.mkdirs()

        project.copy {
            it.from(config.unarchive())
            it.into(config.expandDir)
        }

        project.exec {
            it.executable = config.bashFile.canonicalPath
            it.args = ['--login', '-c', 'exit 0',]
        }.rethrowFailure()
    }
}