package com.github.jishida.gradle.shellscript.util

import org.gradle.internal.os.OperatingSystem

final class EnvironmentUtils {
    final static boolean windows = OperatingSystem.current() == OperatingSystem.WINDOWS
}