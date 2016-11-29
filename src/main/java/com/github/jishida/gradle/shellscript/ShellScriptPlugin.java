package com.github.jishida.gradle.shellscript;

import com.github.jishida.gradle.shellscript.tasks.Msys2Setup;
import com.github.jishida.gradle.shellscript.tasks.ShellScript;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static com.github.jishida.gradle.commons.util.EnvironmentUtils.isWindows;
import static com.github.jishida.gradle.commons.util.ProjectUtils.*;
import static com.github.jishida.gradle.shellscript.ShellScriptStrings.EXTENSION_NAME;

public class ShellScriptPlugin implements Plugin<Project> {
    @Override
    public void apply(final Project project) {
        final ShellScriptExtension extension = addExtension(project, EXTENSION_NAME, ShellScriptExtension.class, project);

        if (isWindows()) {
            createTask(project, Msys2Setup.TASK_NAME, Msys2Setup.class);
        }

        addTaskType(project, ShellScript.class);

        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                extension.configure();
            }
        });
    }
}
