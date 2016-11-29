package com.github.jishida.gradle.shellscript

import static com.github.jishida.gradle.shellscript.TestStrings.TEST_CACHE_PATH

class ShellScriptTest extends AbstractShellScriptTest {
    final static TASK_NAME = 'runShellScript'

    private def getMsys2Dir() {
        new File(projectDir, 'cache dir/msys32')
    }

    def setup() {
        buildFile << """
        // ShellScriptTest
        task ${TASK_NAME}(type: ShellScript) << {
            execResult.rethrowFailure()
        }
        """.stripIndent()
    }

    private def getScriptFile() {
        new File(projectDir, 'test.sh')
    }

    private def getOutputFile() {
        new File(projectDir, 'output file')
    }

    def 'run shell script from file with setup'() {
        buildFile << """
        // run shell script from file with setup
        ${TASK_NAME}.scriptFile = file('test.sh')
        """.stripIndent()

        scriptFile << '''
        #!/usr/bin/env bash
        exit 0
        '''.stripIndent()

        when:
        def result = runTasks(TASK_NAME)

        then:
        result.success
        result.wasExecuted(TASK_NAME)
    }

    def 'run shell script from file'() {
        buildFile << """
        // run shell script from file
        shellscript {
            msys2 {
                cacheDir = file('../../../../${TEST_CACHE_PATH}')
                setup = false
            }
        }
        ${TASK_NAME}.scriptFile = file('test.sh')
        ${TASK_NAME}.args = ['output file']
        ${TASK_NAME}.outputs.file file('output file')
        """.stripIndent()

        scriptFile << '''
        #!/usr/bin/env bash
        touch "$1"
        '''.stripIndent()

        when:
        def result = runTasks(TASK_NAME)

        then:
        result.success
        result.wasExecuted(TASK_NAME)
        outputFile.file

        when:
        result = runTasks(TASK_NAME)

        then:
        result.success
        result.wasUpToDate(TASK_NAME)

        when:
        outputFile.delete()

        then:
        !outputFile.exists()

        when:
        result = runTasks(TASK_NAME)

        then:
        result.success
        result.wasExecuted(TASK_NAME)

        when:
        scriptFile.setLastModified(System.currentTimeMillis())
        result = runTasks(TASK_NAME)

        then:
        result.success
        result.wasExecuted(TASK_NAME)

        when:
        buildFile << """
        ${TASK_NAME}.workingDir = file('working_dir')
        """.stripIndent()
        result = runTasks(TASK_NAME)

        then:
        new File(projectDir, 'working_dir/output file').file

        when:
        outputFile.delete()

        then:
        !outputFile.exists()

        when:
        scriptFile << '''
        exit 1
        '''.stripIndent()
        result = runTasks(TASK_NAME)

        then:
        !result.success
    }

    def 'run shell script from text'() {
        buildFile << """
        // run shell script from text
        shellscript {
            msys2 {
                cacheDir = file('../../../../${TEST_CACHE_PATH}')
                setup = false
            }
        }
        """.stripIndent()

        when:
        def result = runTasks(TASK_NAME)

        then:
        !result.success

        when:
        buildFile << """
        // run shell script from text
        ${TASK_NAME}.scriptText = 'exit 0'
        """.stripIndent()
        result = runTasks(TASK_NAME)

        then:
        result.success
        result.wasExecuted(TASK_NAME)

        when:
        buildFile << """
        ${TASK_NAME}.scriptText = 'exit 1'
        """.stripIndent()
        result = runTasks(TASK_NAME)

        then:
        !result.success
    }
}