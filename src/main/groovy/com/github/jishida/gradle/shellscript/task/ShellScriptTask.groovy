package com.github.jishida.gradle.shellscript.task

import com.github.jishida.gradle.shellscript.Msys2Info
import com.github.jishida.gradle.shellscript.ShellScriptInfo
import org.gradle.api.Task

import static com.github.jishida.gradle.shellscript.util.ProjectUtils.getShellScriptExtension

trait ShellScriptTask implements Task {
    ShellScriptInfo getShellScriptInfo() {
        getShellScriptExtension(project)?.info
    }

    Msys2Info getMsys2Info(){
        getShellScriptInfo()?.msys2
    }
}