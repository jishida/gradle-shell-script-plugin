package com.github.jishida.gradle.shellscript

import static com.github.jishida.gradle.shellscript.ShellScriptStrings.Tasks

class Msys2DownloadTest extends AbstractShellScriptTest {
    private def getArchiveFile() {
        new File(projectDir, "cache dir/archive/$MSYS2_TEST_ARCHIVE_NAME")
    }

    def 'run `msys2Download` task'() {
        when:
        def result = runTasks(Tasks.MSYS2_DOWNLOAD)
        result.rethrowFailure()

        then:
        result.success
        result.wasExecuted(Tasks.MSYS2_DOWNLOAD)
        archiveFile.file

        when:
        result = runTasks(Tasks.MSYS2_DOWNLOAD)
        result.rethrowFailure()

        then:
        result.success
        result.wasUpToDate(Tasks.MSYS2_DOWNLOAD)
        archiveFile.file

        when:
        archiveFile.delete()

        then:
        !archiveFile.exists()

        when:
        result = runTasks(Tasks.MSYS2_DOWNLOAD)
        result.rethrowFailure()

        then:
        result.success
        result.wasExecuted(Tasks.MSYS2_DOWNLOAD)
        archiveFile.file

        when:
        buildFile << '''
        // run `msys2Download` task
        shellscript.msys2.verify = false
        '''
        result = runTasks(Tasks.MSYS2_DOWNLOAD)
        result.rethrowFailure()

        then:
        result.success
        result.wasExecuted(Tasks.MSYS2_DOWNLOAD)
        archiveFile.file
    }

    def 'run `msys2Download` task with no setup'() {
        setup:
        buildFile << """
        // msys2Download` task with no setup
        shellscript.msys2.setup = false
        """.stripIndent()

        when:
        def result = runTasks(Tasks.MSYS2_DOWNLOAD)
        result.rethrowFailure()

        then:
        result.success
        result.wasExecuted(Tasks.MSYS2_DOWNLOAD)
        !archiveFile.exists()

        when:
        result = runTasks(Tasks.MSYS2_DOWNLOAD)
        result.rethrowFailure()

        then:
        result.success
        result.wasUpToDate(Tasks.MSYS2_DOWNLOAD)
        !archiveFile.exists()
    }

    def 'failure because URL is missing'() {
        buildFile << '''
        // failure because URL is missing
        shellscript.msys2.distUrl = 'http://localhost:8080/dist/missing.tar.xz'
        '''.stripIndent()
        when:
        final def result = runTasks(Tasks.MSYS2_DOWNLOAD)

        then:
        result.failure
    }

    def 'failure because of unmached hash'() {
        buildFile << '''
        // failure because of unmached hash
        shellscript.msys2.sha256 = 'invalid string'
        '''.stripIndent()
        when:
        final def result = runTasks(Tasks.MSYS2_DOWNLOAD)

        then:
        result.failure
    }
}