package com.github.jishida.gradle.shellscript

import com.github.jishida.gradle.shellscript.archive.TarXZUnarchiver
import nebula.test.PluginProjectSpec
import org.gradle.api.Project
import org.gradle.api.internal.project.AbstractProject

import static com.github.jishida.gradle.shellscript.ShellScriptStrings.*
import static com.github.jishida.gradle.shellscript.util.EnvironmentUtils.windows

class ShellScriptPluginTest extends PluginProjectSpec {
    @Override
    String getPluginName() {
        'com.github.jishida.shellscript'
    }

    def 'check default `shellscript` extension'() {
        setup:
        project.apply plugin: pluginName
        final ext = project.extensions.getByType(ShellScriptExtension)
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
            final subproject = createSubproject(project, subprojectName)
            project.subprojects.add(subproject)
        }
        final sub1 = project.project(':sub1')
        final sub2 = project.project(':sub2')
        final sub3 = project.project(':sub3')
        final sub4 = project.project(':sub4')

        project.apply plugin: pluginName
        sub1.apply plugin: pluginName
        sub2.apply plugin: pluginName
        sub3.apply plugin: pluginName

        when:
        final ext = project.extensions.findByType(ShellScriptExtension)
        final sub1Ext = sub1.extensions.findByType(ShellScriptExtension)
        final sub2Ext = sub2.extensions.findByType(ShellScriptExtension)
        final sub3Ext = sub3.extensions.findByType(ShellScriptExtension)
        final sub4ExtNull = sub4.extensions.findByType(ShellScriptExtension)

        then:
        ext != null
        sub1Ext != null
        sub2Ext != null
        sub3Ext != null
        sub4ExtNull == null

        when:
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
        evaluate(sub4)

        final sub4Ext = sub4.extensions.findByType(ShellScriptExtension)

        then:
        sub4Ext != null

        when:
        final config = ext.config
        final sub1Config = sub1Ext.config
        final sub2Config = sub2Ext.config
        final sub3Config = sub3Ext.config
        final sub4Config = sub4Ext.config

        then:
        config.msys2.cacheDir == new File(projectDir, 'temp')
        config.msys2.bashFile == new File(projectDir, "temp/local/msys64/usr/bin/bash.exe")
        config.msys2.distUrl == new URL('http://repo.msys2.org/distrib/i686/msys2-base-x86_64-20161025.tar.xz')
        !config.msys2.verify
        config.msys2.hash == 'bb1f1a0b35b3d96bf9c15092da8ce969a84a134f7b08811292fbc9d84d48c65d'

        verifyCacheProject(config.msys2, config.msys2)
        verifyCacheProject(config.msys2, sub1Config.msys2)
        verifyCacheProject(config.msys2, sub2Config.msys2)
        verifyCacheProject(sub4Config.msys2, sub3Config.msys2)

        when:
        final msys2Setup = project.tasks.findByName(Tasks.MSYS2_SETUP)
        final sub1Msys2Setup = sub1.tasks.findByName(Tasks.MSYS2_SETUP)
        final sub2Msys2Setup = sub2.tasks.findByName(Tasks.MSYS2_SETUP)
        final sub3Msys2Setup = sub3.tasks.findByName(Tasks.MSYS2_SETUP)
        final sub4Msys2Setup = sub4.tasks.findByName(Tasks.MSYS2_SETUP)

        then:
        windows  || msys2Setup == null
        windows  || sub1Msys2Setup == null
        windows  || sub2Msys2Setup == null
        windows  || sub3Msys2Setup == null
        windows  || sub4Msys2Setup == null

        !windows || msys2Setup.dependsOn.size() == 2
        !windows || msys2Setup.dependsOn.contains(Tasks.MSYS2_DOWNLOAD)

        !windows || sub1Msys2Setup.dependsOn.size() == (windows ? 3 : 2)
        !windows || sub1Msys2Setup.dependsOn.contains(Tasks.MSYS2_DOWNLOAD)
        !windows || sub1Msys2Setup.dependsOn.contains(msys2Setup)

        !windows || sub2Msys2Setup.dependsOn.size() == (windows ? 3 : 2)
        !windows || sub2Msys2Setup.dependsOn.contains(Tasks.MSYS2_DOWNLOAD)
        !windows || sub2Msys2Setup.dependsOn.contains(msys2Setup)

        !windows || sub3Msys2Setup.dependsOn.size() == 3
        !windows || sub3Msys2Setup.dependsOn.contains(Tasks.MSYS2_DOWNLOAD)
        !windows || sub3Msys2Setup.dependsOn.contains(sub4Msys2Setup)

        !windows || sub4Msys2Setup.dependsOn.size() == 2
        !windows || sub4Msys2Setup.dependsOn.contains(Tasks.MSYS2_DOWNLOAD)
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