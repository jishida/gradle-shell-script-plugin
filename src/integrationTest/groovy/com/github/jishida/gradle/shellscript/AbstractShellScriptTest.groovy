package com.github.jishida.gradle.shellscript

import com.github.jishida.gradle.shellscript.task.Msys2Download
import com.github.jishida.gradle.shellscript.task.Msys2Setup
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import nebula.test.IntegrationSpec
import spock.lang.Shared

import static com.github.jishida.gradle.shellscript.ShellScriptStrings.Tasks
import static com.github.jishida.gradle.shellscript.util.EnvironmentUtils.windows
import static com.github.jishida.gradle.shellscript.util.FileUtils.deleteFile
import static com.github.jishida.gradle.shellscript.util.FileUtils.verifyMsys2Archive

abstract class AbstractShellScriptTest extends IntegrationSpec {
    final static def MSYS2_TEST_ARCHIVE_NAME = 'msys2-base-i686-20161025.tar.xz'
    final static def MSYS2_TEST_ARCHIVE_HASH = '8bafd3d52f5a51528a8671c1cae5591b36086d6ea5b1e76e17e390965cf6768f'
    final static def MSYS2_TEST_DIST_URL = "http://localhost:8080/dist/$MSYS2_TEST_ARCHIVE_NAME"

    private final static def CACHE_PATH = 'build/cache/gradle-shell-script-plugin'
    private final static def MSYS2_DIST_URL = "http://repo.msys2.org/distrib/i686/$MSYS2_TEST_ARCHIVE_NAME"
    private final static def MSYS2_ARCHIVE_PATH = "$CACHE_PATH/dist/$MSYS2_TEST_ARCHIVE_NAME"

    @Shared
    private File _msys2ArchiveFile
    @Shared
    private HttpServer server

    def setupSpec() {
        def download = false
        _msys2ArchiveFile = new File(MSYS2_ARCHIVE_PATH).canonicalFile
        if (msys2ArchiveFile.exists()) {
            if (msys2ArchiveFile.file) {
                download = verifyMsys2Archive(msys2ArchiveFile, MSYS2_TEST_ARCHIVE_HASH)
            }
        }
        if (!download) {
            deleteFile(msys2ArchiveFile)
            msys2ArchiveFile.parentFile.mkdirs()
            new URL(MSYS2_DIST_URL).openStream().withStream {
                msys2ArchiveFile << it
            }
            if (!verifyMsys2Archive(msys2ArchiveFile, MSYS2_TEST_ARCHIVE_HASH)) {
                throw new IllegalStateException('failed to download msys2 archive')
            }
        }

        server = HttpServer.create(new InetSocketAddress(8080), 0)
        server.createContext("/dist/$MSYS2_TEST_ARCHIVE_NAME", new HttpHandler() {
            @Override
            void handle(HttpExchange exchange) throws IOException {
                if (exchange.requestMethod == 'GET') {
                    exchange.responseHeaders.add('Content-Type', 'application/octet-stream')
                    exchange.sendResponseHeaders(200, msys2ArchiveFile.size())
                    exchange.responseBody.withStream { response ->
                        msys2ArchiveFile.withInputStream {
                            response << it
                        }
                    }
                } else {
                    exchange.sendResponseHeaders(400, 0)
                }
            }
        })
        server.start()
    }

    def cleanupSpec() {
        server.stop(0)
    }

    def setup() {
        buildFile << """
        buildscript {
            dependencies {
                classpath gradleApi()
                classpath files('../../../libs/gradle-shell-script-plugin-${getJarVersion()}.jar')
            }
        }

        apply plugin: 'com.github.jishida.shellscript'

        shellscript {
            msys2 {
                sha256 = '${MSYS2_TEST_ARCHIVE_HASH}'
                distUrl = '${MSYS2_TEST_DIST_URL}'
                cacheDir = file('cache dir')
            }
        }
        """.stripIndent()

        if (!windows) {
            buildFile << """
            // add windows tasks if not windows
            import com.github.jishida.gradle.shellscript.task.*
            task ${Tasks.MSYS2_DOWNLOAD}(type: ${Msys2Download.simpleName})
            task ${Tasks.MSYS2_SETUP}(type: ${Msys2Setup.simpleName}, dependsOn: ${Tasks.MSYS2_DOWNLOAD})
            """.stripIndent()
        }
    }

    def cleanup() {
        projectDir.deleteDir()
    }

    File getMsys2ArchiveFile() {
        _msys2ArchiveFile
    }

    private static String getJarVersion() {
        final def props = new Properties()
        new File('gradle.properties').withInputStream {
            props.load(it)
        }
        props.get('version')
    }
}