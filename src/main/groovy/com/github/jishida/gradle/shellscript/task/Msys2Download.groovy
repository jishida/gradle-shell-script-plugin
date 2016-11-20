package com.github.jishida.gradle.shellscript.task

import com.github.jishida.gradle.shellscript.Msys2Config
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import static com.github.jishida.gradle.shellscript.util.FileUtils.deleteFile
import static com.github.jishida.gradle.shellscript.util.FileUtils.verifyMsys2Archive

class Msys2Download extends AbstractShellScriptTask {
    Msys2Config getConfig() {
        shellScriptConfig?.msys2
    }

    @Input
    URL getDistUrl() {
        config?.distUrl
    }

    @OutputFile
    File getArchiveFile() {
        config?.archiveFile
    }

    @Input
    boolean isSetup() {
        config?.setup
    }

    @Input
    boolean isVerify() {
        config?.verify
    }

    @Input
    String getHash() {
        if (config == null) return null
        config.verify ? config.hash : ''
    }

    @TaskAction
    protected void download() {
        if (!config.setup) return

        deleteFile(config.archiveFile)
        config.archiveFile.parentFile.mkdirs()
        config.distUrl.openStream().withStream {
            config.archiveFile << it
        }

        if (config.verify && !verifyMsys2Archive(config.archiveFile, config.hash)) {
            throw new UnsupportedOperationException("failed to verify `${config.archiveFile}`.")
        }
    }
}