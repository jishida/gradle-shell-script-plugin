package com.github.jishida.gradle.shellscript.archive

import org.gradle.api.Project
import org.gradle.api.file.FileTree

interface Unarchiver {
    FileTree getFileTree(Project project, File archiveFile)
}