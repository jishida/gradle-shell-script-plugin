package com.github.jishida.gradle.shellscript.archive

import org.gradle.api.Project
import org.gradle.api.file.FileTree

class ZipUnarchiver implements Unarchiver {
    @Override
    FileTree getFileTree(final Project project, final File archiveFile) {
        project.zipTree(archiveFile)
    }
}