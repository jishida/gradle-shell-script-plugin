package com.github.jishida.gradle.shellscript.task

import com.github.jishida.gradle.shellscript.Msys2CacheInfo
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import static com.github.jishida.gradle.shellscript.util.FileUtils.deleteFile
import static com.github.jishida.gradle.shellscript.util.FileUtils.verifyMsys2Archive

class Msys2Download extends AbstractShellScriptTask {
    Msys2CacheInfo getCacheInfo() {
        msys2Info?.cache
    }

    @Input
    URL getDistUrl() {
        cacheInfo?.distUrl
    }

    @OutputFile
    File getArchiveFile() {
        cacheInfo?.archiveFile
    }

    @Input
    boolean isSetup() {
        msys2Info?.setup
    }

    @Input
    boolean isVerify() {
        cacheInfo?.verify
    }

    @Input
    String getHash() {
        if (shellScriptInfo == null) return null
        cacheInfo.verify ? cacheInfo.hash : ''
    }

    @TaskAction
    protected void download() {
        if (!msys2Info.setup) return

        deleteFile(cacheInfo.archiveFile)
        cacheInfo.archiveFile.parentFile.mkdirs()
        cacheInfo.distUrl.openStream().withStream {
            cacheInfo.archiveFile << it
        }

        if (cacheInfo.verify && !verifyMsys2Archive(cacheInfo.archiveFile, cacheInfo.hash)) {
            throw new UnsupportedOperationException("failed to verify `${cacheInfo.archiveFile}`.")
        }
    }
}