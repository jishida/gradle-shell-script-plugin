package com.github.jishida.gradle.shellscript.task

import com.github.jishida.gradle.shellscript.Msys2CacheInfo
import org.gradle.api.tasks.*

class Msys2Setup extends AbstractShellScriptTask {
    Msys2CacheInfo getCacheInfo() {
        msys2Info?.cache
    }

    @InputFile
    File getArchiveFile() {
        cacheInfo?.archiveFile
    }

    @OutputDirectory
    File getOutputDir() {
        cacheInfo?.expandDir
    }

    @Input
    boolean isSetup() {
        msys2Info?.setup
    }

    @Input
    Class getUnarchiverClass() {
        cacheInfo?.unarchiverClass
    }

    @OutputFile
    File getBashFile() {
        cacheInfo?.bashFile
    }

    @TaskAction
    void setup() {
        if (!msys2Info.setup) return

        if (!cacheInfo.bashFile.file) {
            cacheInfo.expandDir.deleteDir()
            cacheInfo.expandDir.mkdirs()

            project.copy {
                it.from(cacheInfo.unarchive())
                it.into(cacheInfo.expandDir)
            }
        }

        project.exec {
            it.executable = cacheInfo.bashFile
            it.args = ['--login', '-c', 'exit 0',]
        }.rethrowFailure()
    }
}