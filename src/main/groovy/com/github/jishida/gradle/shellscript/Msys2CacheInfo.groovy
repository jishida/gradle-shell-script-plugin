package com.github.jishida.gradle.shellscript

import com.github.jishida.gradle.shellscript.archive.Unarchiver
import groovy.transform.PackageScope
import org.gradle.api.Project
import org.gradle.api.file.FileTree

import static com.github.jishida.gradle.shellscript.ShellScriptStrings.*
import static com.github.jishida.gradle.shellscript.util.URLUtils.findFileName

class Msys2CacheInfo {
    private final Unarchiver _unarchiver

    final Project project

    final URL distUrl
    final File workingDir
    final File expandDir
    final File bashFile
    final File archiveFile
    final Class<? extends Unarchiver> unarchiverClass
    final String hash
    final boolean verify
    final boolean ignoreCertificate

    @PackageScope
    Msys2CacheInfo(final Msys2Spec spec) {
        project = spec.project
        distUrl = new URL(spec.distUrl ?: DEFAULT_MSYS2_DIST_URL)
        workingDir = spec.workingDirOrDefault
        expandDir = new File(workingDir, 'local')
        final bashPath = spec.bashPath ?: DEFAULT_MSYS2_BASH_PATH
        final tempBash = new File(bashPath)
        bashFile = (tempBash.absolute ? tempBash : new File(expandDir, bashPath)).canonicalFile
        archiveFile = new File(workingDir, "archive/${findFileName(distUrl) ?: 'msys2_archive.unknown'}")
        unarchiverClass = spec.unarchiverClass
        hash = spec.sha256
        verify = spec.verify
        ignoreCertificate = spec.ignoreCertificate
        _unarchiver = unarchiverClass.getConstructor().newInstance()
    }

    FileTree unarchive() {
        _unarchiver.getFileTree(project, archiveFile)
    }
}