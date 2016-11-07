package com.github.jishida.gradle.shellscript

class ShellScriptTest extends AbstractShellScriptTest {
    final static def TASK_NAME = 'runShellScript'

    def setup() {
        buildFile << """
        // ShellScriptTest
        task $TASK_NAME(type: ShellScript) {
            scriptFile = file('test.sh')
        }
        """
    }

    private def getScriptFile() {
        new File(projectDir, 'test.sh')
    }

    private def getOutputFile() {
        new File(projectDir, 'test.out')
    }

    def 'run shell script'() {
        buildFile << """
        // run success script
        ${TASK_NAME}.outputs.file file('test.sh')
        """.stripIndent()

        scriptFile << '''
        #!/usr/bin/env bash
        touch test.out
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
        scriptFile << '''
        exit 0
        '''.stripIndent()
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
        result.success
        result.wasExecuted(TASK_NAME)
        new File(projectDir, 'working_dir/test.out').file
    }

    def 'exit shell script on error'() {
        scriptFile << '''
        #!/usr/bin/env bash
        exit 1
        '''

        when:
        def result = runTasks(TASK_NAME)

        then:
        result.failure
    }
}