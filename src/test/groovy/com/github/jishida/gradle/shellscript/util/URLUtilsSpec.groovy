package com.github.jishida.gradle.shellscript.util

import spock.lang.Specification

class URLUtilsSpec extends Specification {
    def 'check `findFileName` results'() {
        expect:
        URLUtils.findFileName(new URL(url)) == name

        where:
        url                                                                                       || name
        'http://jishida.github.com/distrib/i686/msys2-base-i686-20160921.tar.xz'                  || 'msys2-base-i686-20160921.tar.xz'
        'http://jishida.github.com/dist/msys2-base-x86_64-20161025.tar.xz?query1=hoge&query2=foo' || 'msys2-base-x86_64-20161025.tar.xz'
        'http://jishida.github.com/dist/directory/'                                               || null
    }
}