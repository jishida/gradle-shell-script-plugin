package com.github.jishida.gradle.shellscript

import com.github.jishida.gradle.shellscript.archive.Unarchiver
import groovy.transform.PackageScope
import org.gradle.api.Project
import org.gradle.api.file.FileTree

import static com.github.jishida.gradle.shellscript.ShellScriptStrings.DEFAULT_MSYS2_BASH_PATH
import static com.github.jishida.gradle.shellscript.ShellScriptStrings.DEFAULT_MSYS2_DIST_URL
import static com.github.jishida.gradle.shellscript.util.ProjectUtils.getBaseMsys2Config
import static com.github.jishida.gradle.shellscript.util.URLUtils.findFileName

class Msys2Config {
    private final Unarchiver unarchiver

    final Project project

    final Project cacheProject

    final URL distUrl
    final File cacheDir
    final File expandDir
    final File bashFile
    final File archiveFile
    final Class<? extends Unarchiver> unarchiverClass
    final String hash
    final boolean verify
    final boolean setup

    @PackageScope
    Msys2Config(final Msys2Extension extension) {
        project = extension.project

        final def config = getBaseMsys2Config(project)

        if (config == null) {
            cacheProject = null
            distUrl = new URL(extension.distUrl ?: DEFAULT_MSYS2_DIST_URL)
            cacheDir = extension.workingDirOrDefault
            expandDir = new File(cacheDir, 'local')
            final def bashPath = extension.bashPath ?: DEFAULT_MSYS2_BASH_PATH
            final def tempBash = new File(bashPath)
            bashFile = tempBash.absolute ? tempBash : new File(expandDir, bashPath)
            archiveFile = new File(cacheDir, "archive/${findFileName(distUrl) ?: 'msys2_archive.unknown'}")
            unarchiverClass = extension.unarchiverClass
            hash = extension.sha256
            verify = extension.verify
            setup = extension.setup
        } else {
            cacheProject = config.cacheProject ?: config.project
            distUrl = config.distUrl
            cacheDir = config.cacheDir
            expandDir = config.expandDir
            bashFile = config.bashFile
            archiveFile = config.archiveFile
            unarchiverClass = config.unarchiverClass
            hash = config.hash
            verify = config.verify
            setup = false
        }
        unarchiver = unarchiverClass.getConstructor().newInstance()
    }

    FileTree unarchive() {
        unarchiver.getFileTree(project, archiveFile)
    }
}