package com.github.jishida.gradle.shellscript;

import static java.lang.String.format;

public class ShellScriptStrings {
    public final static String GROUP_ID = "com.github.jishida.gradle";
    public final static String ARTIFACT_ID = "gradle-shell-script-plugin";
    public final static String PLUGIN_ID = "com.github.jishida.shellscript";
    public final static String EXTENSION_NAME = "shellscript";

    public final static String DEFAULT_UNIX_SHELL = "bash";

    public final static String DEFAULT_MSYS2_DIST_URL = "http://repo.msys2.org/distrib/i686/msys2-base-i686-20161025.tar.xz";
    //public final static String DEFAULT_MSYS2_DIST_URL = "https://sourceforge.net/projects/msys2/files/Base/i686/msys2-base-i686-20161025.tar.xz/download";
    public final static String DEFAULT_MSYS2_CACHE_PATH = format(".gradle/%s/%s", GROUP_ID, ARTIFACT_ID);
    public final static String DEFAULT_MSYS2_BASH_PATH = "msys32/usr/bin/bash.exe";

    public final static String DEFAULT_SHELL_SCRIPT_M_SYSTEM = "MSYS";

    public final static String TEMP_PATH = format("tmp/%s/%s", GROUP_ID, ARTIFACT_ID);
}
