package com.github.jishida.gradle.shellscript.util

import org.gradle.internal.os.OperatingSystem

import java.util.regex.Pattern

final class EnvironmentUtils {
    private static class Regex {
        final static JAVA_VERSION = Pattern.compile('^([0-9]+)\\.([0-9]+)\\.([0-9]+)_([0-9]+).*$')
    }

    final static boolean windows = OperatingSystem.current() == OperatingSystem.WINDOWS

    static int[] getJavaVersion() {
        final versionString = System.getProperty('java.version')
        if (versionString == null) return null
        final matcher = Regex.JAVA_VERSION.matcher(versionString)
        if (!matcher.matches()) return null
        final version = new int[4]
        version[0] = Integer.parseInt(matcher.group(1))
        version[1] = Integer.parseInt(matcher.group(2))
        version[2] = Integer.parseInt(matcher.group(3))
        version[3] = Integer.parseInt(matcher.group(4))
        version
    }
}