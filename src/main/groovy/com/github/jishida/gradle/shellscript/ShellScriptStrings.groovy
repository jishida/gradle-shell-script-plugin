package com.github.jishida.gradle.shellscript

final class ShellScriptStrings {
    final static String PLUGIN_ID = 'com.github.jishida.shellscript'
    final static String EXTENSION_NAME = 'shellscript'

    final static String DEFAULT_UNIX_SHELL = 'bash'

    final static String DEFAULT_MSYS2_DIST_URL = 'http://repo.msys2.org/distrib/i686/msys2-base-i686-20161025.tar.xz'
    //final static String DEFAULT_MSYS2_DIST_URL = 'https://sourceforge.net/projects/msys2/files/Base/i686/msys2-base-i686-20161025.tar.xz/download'
    final static String DEFAULT_MSYS2_CACHE_PATH = '.gradle/msys2'
    final static String DEFAULT_MSYS2_BASH_PATH = 'msys32/usr/bin/bash.exe'

    final static String DEFAULT_SHELL_SCRIPT_M_SYSTEM = 'MSYS'

    final static class Tasks {
        final static String MSYS2_DOWNLOAD = 'msys2Download'
        final static String MSYS2_SETUP = 'msys2Setup'
    }
}