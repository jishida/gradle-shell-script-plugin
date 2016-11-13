package com.github.jishida.gradle.shellscript

import com.github.jishida.gradle.shellscript.archive.*
import groovy.transform.PackageScope
import org.gradle.api.Project

import static com.github.jishida.gradle.shellscript.ShellScriptStrings.*

class Msys2Extension {
    private final Map<String, Class<? extends Unarchiver>> unarchiverMap = [
            'tar'    : TarUnarchiver,
            'tar.bz2': TarBzip2Unarchiver,
            'tar.gz' : TarGzipUnarchiver,
            'tar.xz' : TarXZUnarchiver,
            'zip'    : ZipUnarchiver,
    ]

    final Project project

    Project cacheProject

    String distUrl = DEFAULT_MSYS2_DIST_URL
    String bashPath = DEFAULT_MSYS2_BASH_PATH
    String archiveType = 'tar.xz'
    String sha256
    boolean verify = true
    File cacheDir
    boolean setup = true

    Msys2Extension(final Project project) {
        this.project = project
        cacheDir = defaultWorkingDir
        cacheProject = project.rootProject
    }

    @PackageScope
    File getWorkingDirOrDefault() {
        (cacheDir ?: defaultWorkingDir).canonicalFile
    }

    private File getDefaultWorkingDir() {
        cacheDir = new File(project.projectDir, DEFAULT_MSYS2_CACHE_PATH)
    }

    void registerUnarchiver(final String archiveType, final Class<? extends Unarchiver> unarchiverClass) {
        if (archiveType == null) {
            throw new IllegalArgumentException('`archiveType` cannot be null.')
        }
        if (unarchiverClass == null) {
            throw new IllegalArgumentException('`unarchiverClass` cannot be null.')
        }
        unarchiverMap[archiveType] = unarchiverClass
    }

    @PackageScope
    Class<? extends Unarchiver> getUnarchiverClass() {
        final def result = unarchiverMap[archiveType]
        if (result == null) {
            throw new UnsupportedOperationException("unknown archive type `$archiveType`")
        }
        result
    }
}