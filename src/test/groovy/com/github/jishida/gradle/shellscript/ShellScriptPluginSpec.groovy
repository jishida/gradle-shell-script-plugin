package com.github.jishida.gradle.shellscript

import com.github.jishida.gradle.shellscript.archive.TarXZUnarchiver
import nebula.test.PluginProjectSpec
import org.gradle.api.Project
import org.gradle.api.internal.project.AbstractProject

import static com.github.jishida.gradle.shellscript.ShellScriptStrings.*
import static com.github.jishida.gradle.shellscript.util.EnvironmentUtils.windows

class ShellScriptPluginSpec extends PluginProjectSpec {
    @Override
    String getPluginName() {
        'com.github.jishida.shellscript'
    }

    def 'check default `shellscript` extension'() {
        setup:
        project.apply plugin: pluginName
        final def ext = project.extensions.getByType(ShellScriptExtension)
        def config = ext.config

        expect:
        config == null
        ext.project == project
        ext.unixShell == 'bash'
        ext.shellArgs == []
        ext.msys2.project == project
        ext.msys2.cacheProject == project
        ext.msys2.distUrl == DEFAULT_MSYS2_DIST_URL
        ext.msys2.bashPath == DEFAULT_MSYS2_BASH_PATH
        ext.msys2.archiveType == 'tar.xz'
        ext.msys2.sha256 == null
        ext.msys2.verify
        ext.msys2.cacheDir == new File(projectDir, DEFAULT_MSYS2_CACHE_PATH)
        ext.msys2.setup

        (config = ext.configure()) != null
        config.project == project
        config.unixShell == 'bash'
        config.shellArgs == []
        config.msys2.project == project
        config.msys2.cacheProject == null
        config.msys2.distUrl == new URL(DEFAULT_MSYS2_DIST_URL)
        config.msys2.cacheDir == new File(projectDir, DEFAULT_MSYS2_CACHE_PATH)
        config.msys2.expandDir == new File(projectDir, "${DEFAULT_MSYS2_CACHE_PATH}/local").canonicalFile
        config.msys2.bashFile == new File(config.msys2.expandDir, DEFAULT_MSYS2_BASH_PATH).canonicalFile
        config.msys2.archiveFile == new File(projectDir, "${DEFAULT_MSYS2_CACHE_PATH}/archive/msys2-base-i686-20160921.tar.xz")
        config.msys2.unarchiverClass == TarXZUnarchiver
        config.msys2.hash == null
        config.msys2.verify
        config.msys2.setup
    }

    def 'check multi-project configrations'() {
        ['sub1', 'sub2', 'sub3', 'sub4'].each { subprojectName ->
            final def subproject = createSubproject(project, subprojectName)
            project.subprojects.add(subproject)
        }
        final def sub1 = project.project(':sub1')
        final def sub2 = project.project(':sub2')
        final def sub3 = project.project(':sub3')
        final def sub4 = project.project(':sub4')

        project.apply plugin: pluginName
        sub1.apply plugin: pluginName
        sub2.apply plugin: pluginName
        sub3.apply plugin: pluginName

        final def ext = project.extensions.getByType(ShellScriptExtension)
        final def sub1Ext = sub1.extensions.getByType(ShellScriptExtension)
        final def sub2Ext = sub2.extensions.getByType(ShellScriptExtension)
        final def sub3Ext = sub3.extensions.getByType(ShellScriptExtension)

        ext.msys2.cacheDir = new File(projectDir, 'temp')
        ext.msys2.bashPath = 'msys64/usr/bin/bash.exe'
        ext.msys2.distUrl = 'http://repo.msys2.org/distrib/i686/msys2-base-x86_64-20161025.tar.xz'
        ext.msys2.verify = false
        ext.msys2.sha256 = 'bb1f1a0b35b3d96bf9c15092da8ce969a84a134f7b08811292fbc9d84d48c65d'
        sub2Ext.msys2.cacheProject = sub1
        sub3Ext.msys2.cacheProject = sub4

        evaluate(project)
        evaluate(sub1)
        evaluate(sub2)
        evaluate(sub3)

        when:
        final def config = ext.config
        final def sub1Config = sub1Ext.config
        final def sub2Config = sub2Ext.config
        final def sub3Config = sub3Ext.config

        then:
        config.msys2.cacheDir == new File(projectDir, 'temp')
        config.msys2.bashFile == new File(projectDir, "temp/local/msys64/usr/bin/bash.exe")
        config.msys2.distUrl == new URL('http://repo.msys2.org/distrib/i686/msys2-base-x86_64-20161025.tar.xz')
        !config.msys2.verify
        config.msys2.hash == 'bb1f1a0b35b3d96bf9c15092da8ce969a84a134f7b08811292fbc9d84d48c65d'

        verifyCacheProject(config.msys2, config.msys2)
        verifyCacheProject(config.msys2, sub1Config.msys2)
        verifyCacheProject(config.msys2, sub2Config.msys2)
        verifyCacheProject(sub3Config.msys2, sub3Config.msys2)

        when:
        final def msys2Setup = project.tasks.findByName(Tasks.MSYS2_SETUP)
        final def sub1Msys2Setup = sub1.tasks.findByName(Tasks.MSYS2_SETUP)
        final def sub2Msys2Setup = sub2.tasks.findByName(Tasks.MSYS2_SETUP)
        final def sub3Msys2Setup = sub3.tasks.findByName(Tasks.MSYS2_SETUP)

        then:
        windows  || msys2Setup == null
        windows  || sub1Msys2Setup == null
        windows  || sub2Msys2Setup == null
        windows  || sub3Msys2Setup == null

        !windows || msys2Setup.dependsOn.size() == 2
        !windows || msys2Setup.dependsOn.contains(Tasks.MSYS2_DOWNLOAD)

        !windows || sub1Msys2Setup.dependsOn.size() == (windows ? 3 : 2)
        !windows || sub1Msys2Setup.dependsOn.contains(Tasks.MSYS2_DOWNLOAD)
        !windows || sub1Msys2Setup.dependsOn.contains(msys2Setup)

        !windows || sub2Msys2Setup.dependsOn.size() == (windows ? 3 : 2)
        !windows || sub2Msys2Setup.dependsOn.contains(Tasks.MSYS2_DOWNLOAD)
        !windows || sub2Msys2Setup.dependsOn.contains(msys2Setup)

        !windows || sub3Msys2Setup.dependsOn.size() == 2
        !windows || sub3Msys2Setup.dependsOn.contains(Tasks.MSYS2_DOWNLOAD)
    }

    def 'when `cacheProject` does not apply `ShellScriptPlugin`'(){
        ['no-apply', 'ref-root', 'ref-no-apply'].each {
            final def subproject = createSubproject(project, it)
            project.subprojects.add(subproject)
        }

        final def noApplyRoot = project
        final def noApply = project.project(':no-apply')
        final def refNoApplyRoot = project.project(':ref-root')
        final def refNoApply = project.project(':ref-no-apply')

        refNoApplyRoot.apply plugin: ShellScriptPlugin
        refNoApply.apply plugin: ShellScriptPlugin

        when:
        def noApplyRootExt = noApplyRoot.extensions.findByType(ShellScriptExtension)
        def noApplyExt = noApply.extensions.findByType(ShellScriptExtension)
        def refNoApplyRootExt = refNoApplyRoot.extensions.findByType(ShellScriptExtension)
        def refNoApplyExt = refNoApply.extensions.findByType(ShellScriptExtension)

        then:
        noApplyRootExt == null
        noApplyExt == null
        refNoApplyRootExt != null
        refNoApplyExt != null

        when:
        refNoApplyRootExt.msys2.cacheProject = noApplyRoot
        refNoApplyExt.msys2.cacheProject = noApply

        evaluate(noApplyRoot)
        evaluate(noApply)
        evaluate(refNoApplyRoot)
        evaluate(refNoApply)

        noApplyRootExt = noApplyRoot.extensions.findByType(ShellScriptExtension)
        noApplyExt = noApply.extensions.findByType(ShellScriptExtension)
        refNoApplyRootExt = refNoApplyRoot.extensions.findByType(ShellScriptExtension)
        refNoApplyExt = refNoApply.extensions.findByType(ShellScriptExtension)

        then:
        noApplyRootExt != null
        noApplyExt == null
        refNoApplyRootExt != null
        refNoApplyExt != null

        noApplyRootExt.config.msys2.cacheProject == null
        refNoApplyRootExt.config.msys2.cacheProject == noApplyRoot
        refNoApplyExt.config.msys2.cacheProject == null

        verifyCacheProject(noApplyRootExt.config.msys2, refNoApplyRootExt.config.msys2)
    }

    private static boolean verifyCacheProject(final Msys2Config cache, final Msys2Config config) {
        cache.cacheProject == null &&
                (config.cacheProject == null || config.cacheProject == cache.project) &&
                (config.cacheProject == null || !config.setup) &&
                cache.distUrl == config.distUrl &&
                cache.cacheDir == config.cacheDir &&
                cache.expandDir == config.expandDir &&
                cache.bashFile == config.bashFile &&
                cache.archiveFile == config.archiveFile &&
                cache.unarchiverClass == config.unarchiverClass &&
                cache.hash == config.hash &&
                cache.verify == config.verify
    }

    private static void evaluate(final Project project) {
        ((AbstractProject) project).evaluate()
    }
}