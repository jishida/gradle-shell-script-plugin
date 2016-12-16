package com.github.jishida.gradle.shellscript

import com.github.jishida.gradle.commons.archive.TarXZUnarchiver
import com.github.jishida.gradle.shellscript.tasks.Msys2Setup
import nebula.test.PluginProjectSpec
import org.gradle.api.Project
import org.gradle.api.internal.file.UnionFileCollection
import org.gradle.api.internal.project.AbstractProject

import static com.github.jishida.gradle.commons.util.EnvironmentUtils.isWindows
import static com.github.jishida.gradle.shellscript.ShellScriptStrings.*

class ShellScriptPluginTest extends PluginProjectSpec {
    @Override
    String getPluginName() {
        PLUGIN_ID
    }

    def 'check default `shellscript` extension'() {
        setup:
        project.apply plugin: pluginName
        final ext = project.extensions.getByType(ShellScriptExtension)
        final nullInfo = ext.info

        expect:
        nullInfo == null
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

        when:
        final info = ext.configure()

        then:
        info != null
        info.project == project
        info.unixShell == 'bash'
        info.shellArgs == []
        info.msys2.project == project
        info.msys2.hasCache()
        info.msys2.cache.distUrl == new URL(DEFAULT_MSYS2_DIST_URL)
        info.msys2.cache.cacheDir == project.file(DEFAULT_MSYS2_CACHE_PATH)
        info.msys2.cache.bashFile == new File(project.file(DEFAULT_MSYS2_CACHE_PATH), DEFAULT_MSYS2_BASH_PATH).canonicalFile
        info.msys2.cache.archiveFile == new File(project.buildDir, "tmp/${GROUP_ID}/${ARTIFACT_ID}/msys2-base-i686-20161025.tar.xz")
        info.msys2.cache.unarchiverClass == TarXZUnarchiver
        info.msys2.cache.hash == null
        info.msys2.cache.verify
        info.msys2.setup
    }

    def 'check multi-project configrations'() {
        final project0 = project
        final project1 = createSubproject(project, 'project1')
        final project2 = createSubproject(project, 'project2')
        final project3 = createSubproject(project, 'project3')
        final project4 = createSubproject(project, 'project4')

        project.apply plugin: pluginName
        project1.apply plugin: pluginName
        project2.apply plugin: pluginName
        project3.apply plugin: pluginName

        when:
        final ext0 = project0.extensions.findByType(ShellScriptExtension)
        final ext1 = project1.extensions.findByType(ShellScriptExtension)
        final ext2 = project2.extensions.findByType(ShellScriptExtension)
        final ext3 = project3.extensions.findByType(ShellScriptExtension)
        final ext4Null = project4.extensions.findByType(ShellScriptExtension)

        then:
        ext0 != null
        ext1 != null
        ext2 != null
        ext3 != null
        ext4Null == null

        when:
        ext0.msys2.cacheDir = project.file('temp')
        ext0.msys2.bashPath = 'msys64/usr/bin/bash.exe'
        ext0.msys2.distUrl = 'http://repo.msys2.org/distrib/i686/msys2-base-x86_64-20161025.tar.xz'
        ext0.msys2.verify = false
        ext0.msys2.sha256 = 'bb1f1a0b35b3d96bf9c15092da8ce969a84a134f7b08811292fbc9d84d48c65d'
        ext2.msys2.cacheProject = project1
        ext3.msys2.cacheProject = project4

        evaluate(project0)
        evaluate(project1)
        evaluate(project2)
        evaluate(project3)
        evaluate(project4)

        final ext4 = project4.extensions.findByType(ShellScriptExtension)

        then:
        ext4 != null

        when:
        final info0 = ext0.info
        final info1 = ext1.info
        final info2 = ext2.info
        final info3 = ext3.info
        final info4 = ext4.info

        then:
        info0.msys2.cache.cacheDir == project.file('temp')
        info0.msys2.cache.bashFile == new File(projectDir, "temp/msys64/usr/bin/bash.exe")
        info0.msys2.cache.distUrl == new URL('http://repo.msys2.org/distrib/i686/msys2-base-x86_64-20161025.tar.xz')
        !info0.msys2.cache.verify
        info0.msys2.cache.hash == 'bb1f1a0b35b3d96bf9c15092da8ce969a84a134f7b08811292fbc9d84d48c65d'

        info0.msys2.hasCache()
        !info1.msys2.hasCache()
        !info2.msys2.hasCache()
        !info3.msys2.hasCache()
        info4.msys2.hasCache()

        info0.msys2.setup
        !info1.msys2.setup
        !info2.msys2.setup
        !info3.msys2.setup
        info4.msys2.setup

        info0.msys2.cache == info1.msys2.cache
        info0.msys2.cache == info2.msys2.cache
        info0.msys2.cache != info3.msys2.cache
        info3.msys2.cache == info4.msys2.cache

        project0 == info0.msys2.cache.project
        project0 == info1.msys2.cache.project
        project0 == info2.msys2.cache.project
        project4 == info3.msys2.cache.project
        project4 == info4.msys2.cache.project

        when:
        final msys2Setup0 = project0.tasks.findByName(Msys2Setup.TASK_NAME)
        final msys2Setup1 = project1.tasks.findByName(Msys2Setup.TASK_NAME)
        final msys2Setup2 = project2.tasks.findByName(Msys2Setup.TASK_NAME)
        final msys2Setup3 = project3.tasks.findByName(Msys2Setup.TASK_NAME)
        final msys2Setup4 = project4.tasks.findByName(Msys2Setup.TASK_NAME)

        then:
        windows ^ msys2Setup0 == null
        !windows || msys2Setup0.dependsOn.findAll { !(it instanceof UnionFileCollection) }.size() == 0

        windows ^ msys2Setup1 == null
        !windows || msys2Setup1.dependsOn.findAll { !(it instanceof UnionFileCollection) }.size() == 1
        !windows || msys2Setup1.dependsOn.contains(msys2Setup0)

        windows ^ msys2Setup2 == null
        !windows || msys2Setup2.dependsOn.findAll { !(it instanceof UnionFileCollection) }.size() == 1
        !windows || msys2Setup2.dependsOn.contains(msys2Setup0)

        windows ^ msys2Setup3 == null
        !windows || msys2Setup3.dependsOn.findAll { !(it instanceof UnionFileCollection) }.size() == 1
        !windows || msys2Setup3.dependsOn.contains(msys2Setup4)

        windows ^ msys2Setup4 == null
        !windows || msys2Setup4.dependsOn.findAll { !(it instanceof UnionFileCollection) }.size() == 0
    }

    private static void evaluate(final Project project) {
        ((AbstractProject) project).evaluate()
    }
}