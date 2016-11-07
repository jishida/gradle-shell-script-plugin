package com.github.jishida.gradle.shellscript.util

import java.util.regex.Pattern

final class URLUtils {
    private final static class Regex {
        final static Pattern FILE_NAME = Pattern.compile('^[^\\/*:?<>"|]+$')
    }

    static String findFileName(URL self) {
        def path = self.getPath()

        def index = path.lastIndexOf('/')
        path = path.substring(index + 1)

        index = path.indexOf('?')
        if (index >= 0) {
            path = path.substring(0, index)
        }
        Regex.FILE_NAME.matcher(path).matches() ? path : null
    }
}