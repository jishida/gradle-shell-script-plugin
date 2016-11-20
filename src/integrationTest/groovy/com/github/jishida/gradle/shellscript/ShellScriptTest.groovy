package com.github.jishida.gradle.shellscript

class ShellScriptTest extends AbstractShellScriptTest {
    final static TASK_NAME = 'runShellScript'

    def setup() {
        buildFile << """
        // ShellScriptTest
        task $TASK_NAME(type: ShellScript)
        """.stripIndent()
    }

    private def getScriptFile() {
        new File(projectDir, 'test.sh')
    }

    private def getOutputFile() {
        new File(projectDir, 'output file')
    }

    def 'run shell script from file'() {
        buildFile << """
        // run shell script from file
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
        result.rethrowFailure()

        then:
        result.success
        result.wasExecuted(TASK_NAME)
        outputFile.file

        when:
        result = runTasks(TASK_NAME)
        result.rethrowFailure()

        then:
        result.success
        result.wasUpToDate(TASK_NAME)

        when:
        outputFile.delete()

        then:
        !outputFile.exists()

        when:
        result = runTasks(TASK_NAME)
        result.rethrowFailure()

        then:
        result.success
        result.wasExecuted(TASK_NAME)

        when:
        scriptFile.setLastModified(System.currentTimeMillis())
        result = runTasks(TASK_NAME)
        result.rethrowFailure()

        then:
        result.success
        result.wasExecuted(TASK_NAME)

        when:
        buildFile << """
        ${TASK_NAME}.workingDir = file('working_dir')
        """.stripIndent()
        result = runTasks(TASK_NAME)
        result.rethrowFailure()

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
        result.failure
    }

    def 'run shell script from text'() {
        when:
        def result = runTasks(TASK_NAME)

        then:
        result.failure

        when:
        buildFile << """
        // run shell script from text
        ${TASK_NAME}.scriptText = 'exit 0'
        """.stripIndent()
        result = runTasks(TASK_NAME)
        result.rethrowFailure()

        then:
        result.success
        result.wasExecuted(TASK_NAME)

        when:
        buildFile << """
        ${TASK_NAME}.scriptText = 'exit 1'
        """.stripIndent()
        result = runTasks(TASK_NAME)

        then:
        result.failure
    }
}