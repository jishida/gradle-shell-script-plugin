package com.github.jishida.gradle.shellscript

import static com.github.jishida.gradle.shellscript.ShellScriptStrings.Tasks
import static com.github.jishida.gradle.shellscript.util.EnvironmentUtils.windows

class Msys2SetupTest extends AbstractShellScriptTest {
    private def getBashFile() {
        new File(projectDir, 'tmp/local/msys32/usr/bin/bash.exe')
    }

    def 'run `msys2Setup` task'() {
        when:
        def result = runTasks(Tasks.MSYS2_SETUP)
        if (windows) {
            result.rethrowFailure()
        }

        then:
        windows  || result.failure
        !windows || result.success
        !windows || result.wasExecuted(Tasks.MSYS2_SETUP)
        bashFile.file

        when:
        if (windows) {
            result = runTasks(Tasks.MSYS2_SETUP)
            result.rethrowFailure()
        }

        then:
        !windows || result.success
        !windows || result.wasUpToDate(Tasks.MSYS2_SETUP)

        when:
        bashFile.delete()

        then:
        !bashFile.exists()

        when:
        if (windows) {
            result = runTasks(Tasks.MSYS2_SETUP)
            result.rethrowFailure()
        }

        then:
        !windows || result.success
        !windows || result.wasExecuted(Tasks.MSYS2_SETUP)
        !windows || bashFile.file

        when:
        if (windows) {
            bashFile.setLastModified(System.currentTimeMillis())
            result = runTasks(Tasks.MSYS2_SETUP)
            result.rethrowFailure()
        }

        then:
        !windows || result.success
        !windows || result.wasUpToDate(Tasks.MSYS2_SETUP)
    }
}