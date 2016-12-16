package com.github.jishida.gradle.shellscript

import com.github.jishida.gradle.commons.archive.TarXZUnarchiver
import groovy.json.JsonSlurper

import static com.github.jishida.gradle.commons.util.EnvironmentUtils.isWindows
import static com.github.jishida.gradle.shellscript.ShellScriptStrings.TEMP_PATH
import static com.github.jishida.gradle.shellscript.TestStrings.TEST_CACHE_PATH
import static com.github.jishida.gradle.shellscript.tasks.Msys2Setup.TASK_NAME

class Msys2SetupTest extends AbstractShellScriptTest {
    private def getBashFile() {
        new File(projectDir, 'cache dir/msys32/usr/bin/bash.exe')
    }

    private def getMsys2Dir() {
        new File(projectDir, 'cache dir/msys32')
    }

    private def getMsys2SetupStatusFile() {
        new File(projectDir, 'cache dir/setup.json')
    }

    private def getDownloadFile() {
        new File(projectDir, "build/${TEMP_PATH}/${TEST_MSYS2_ARCHIVE_NAME}")
    }

    def 'check `msys2Setup` task'() {
        when:
        def result = runTasks(TASK_NAME)
        def status = new JsonSlurper().parse(msys2SetupStatusFile)

        then:
        !windows ^ result.success
        !windows || result.wasExecuted(TASK_NAME)
        msys2Dir.directory
        msys2SetupStatusFile.file
        bashFile.file
        downloadFile.file
        status == [
                setup: true,
                distUrl: TEST_MSYS2_DIST_URL,
                unarchiverClass: TarXZUnarchiver.canonicalName,
                bashFile: bashFile.canonicalPath,
                verify: true,
                ignoreCertificate: false,
                expandDirName: 'msys32',
                hash: '8bafd3d52f5a51528a8671c1cae5591b36086d6ea5b1e76e17e390965cf6768f',
        ]

        when:
        if (windows) {
            result = runTasks(TASK_NAME)
        }

        then:
        !windows || result.success
        !windows || result.wasUpToDate(TASK_NAME)

        when:
        bashFile.delete()

        then:
        !bashFile.exists()

        when:
        result = runTasks(TASK_NAME)
        status = new JsonSlurper().parse(msys2SetupStatusFile)

        then:
        !windows ^ result.success
        !windows || result.wasExecuted(TASK_NAME)
        msys2Dir.directory
        msys2SetupStatusFile.file
        bashFile.file
        downloadFile.file
        status == [
                setup: true,
                distUrl: TEST_MSYS2_DIST_URL,
                unarchiverClass: TarXZUnarchiver.canonicalName,
                bashFile: bashFile.canonicalPath,
                verify: true,
                ignoreCertificate: false,
                expandDirName: 'msys32',
                hash: '8bafd3d52f5a51528a8671c1cae5591b36086d6ea5b1e76e17e390965cf6768f',
        ]

        when:
        if (windows) {
            bashFile.setLastModified(System.currentTimeMillis())
            result = runTasks(TASK_NAME)
        }

        then:
        !windows || result.success
        !windows || result.wasExecuted(TASK_NAME)
    }

    def 'check verifying'(){
        when:
        buildFile << '''
        shellscript.msys2.sha256 = 'invalid value'
        '''.stripIndent()
        def result = runTasks(TASK_NAME)
        def status = new JsonSlurper().parse(msys2SetupStatusFile)

        then:
        !result.success
        msys2Dir.directory
        msys2SetupStatusFile.file
        !bashFile.file
        downloadFile.file
        status == [
                setup: true,
                distUrl: TEST_MSYS2_DIST_URL,
                unarchiverClass: TarXZUnarchiver.canonicalName,
                bashFile: bashFile.canonicalPath,
                verify: true,
                ignoreCertificate: false,
                expandDirName: 'msys32',
                hash: 'invalid value',
        ]

        when:
        buildFile << '''
        shellscript.msys2.verify = false
        '''.stripIndent()
        result = runTasks(TASK_NAME)
        status = new JsonSlurper().parse(msys2SetupStatusFile)

        then:
        !windows ^ result.success
        msys2Dir.directory
        msys2SetupStatusFile.file
        bashFile.file
        downloadFile.file
        status == [
                setup: true,
                distUrl: TEST_MSYS2_DIST_URL,
                unarchiverClass: TarXZUnarchiver.canonicalName,
                bashFile: bashFile.canonicalPath,
                verify: false,
                ignoreCertificate: false,
                expandDirName: 'msys32',
        ]
    }

    def 'disable to setup'(){
        when:
        buildFile << """
        shellscript {
            msys2 {
                cacheDir = file('../../../../${TEST_CACHE_PATH}')
                setup = false
            }
        }
        """.stripIndent()
        def result = runTasks(TASK_NAME)

        then:
        !windows ^ result.success
        !windows || result.wasExecuted(TASK_NAME)
    }
}