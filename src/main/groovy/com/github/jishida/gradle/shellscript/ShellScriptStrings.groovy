package com.github.jishida.gradle.shellscript

final class ShellScriptStrings {
    final static PLUGIN_ID = 'com.github.jishida.shellscript'
    final static EXTENSION_NAME = 'shellscript'

    final static DEFAULT_UNIX_SHELL = 'bash'

    final static DEFAULT_MSYS2_DIST_URL = 'http://repo.msys2.org/distrib/i686/msys2-base-i686-20160921.tar.xz'
    final static DEFAULT_MSYS2_CACHE_PATH = '.gradle/msys2'
    final static DEFAULT_MSYS2_BASH_PATH = 'msys32/usr/bin/bash.exe'

    final static DEFAULT_SHELL_SCRIPT_M_SYSTEM = 'MSYS'

    final static class Tasks {
        final static MSYS2_DOWNLOAD = 'msys2Download'
        final static MSYS2_SETUP = 'msys2Setup'
    }
}