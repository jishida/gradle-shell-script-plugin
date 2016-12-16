package com.github.jishida.gradle.shellscript

import com.github.jishida.gradle.shellscript.tasks.Msys2Setup
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import nebula.test.IntegrationSpec
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import org.gradle.internal.impldep.org.apache.http.client.HttpResponseException
import spock.lang.Shared

import static ShellScriptUtils.verifyMsys2Archive
import static com.github.jishida.gradle.commons.util.EnvironmentUtils.isWindows
import static com.github.jishida.gradle.commons.util.IOUtils.deleteFile
import static com.github.jishida.gradle.commons.util.NetUtils.downloadFile
import static com.github.jishida.gradle.shellscript.ShellScriptStrings.*
import static com.github.jishida.gradle.shellscript.TestStrings.TEST_CACHE_PATH

abstract class AbstractShellScriptTest extends IntegrationSpec {
    final static TEST_MSYS2_ARCHIVE_NAME = 'msys2-base-i686-20161025.tar.xz'
    final static TEST_MSYS2_ARCHIVE_HASH = '8bafd3d52f5a51528a8671c1cae5591b36086d6ea5b1e76e17e390965cf6768f'
    final static TEST_MSYS2_DIST_URL = "http://localhost:8080/dist/$TEST_MSYS2_ARCHIVE_NAME"

    @Shared
    private HttpServer server

    @Shared
    private File _testBashFile

    def File getTestBashFile(){
        _testBashFile
    }

    def setupSpec() {
        def download = false
        final testCacheDir = new File(TEST_CACHE_PATH).canonicalFile
        final msys2ArchiveFile = new File(testCacheDir, TEST_MSYS2_ARCHIVE_NAME)
        _testBashFile = new File(testCacheDir, 'msys32/usr/bin/bash.exe')
        if (msys2ArchiveFile.file) {
            download = verifyMsys2Archive(msys2ArchiveFile, TEST_MSYS2_ARCHIVE_HASH)
        }
        if (!download) {
            deleteFile(testCacheDir)
            testCacheDir.mkdirs()
            try {
                downloadFile(new URL("http://repo.msys2.org/distrib/i686/${TEST_MSYS2_ARCHIVE_NAME}"), msys2ArchiveFile)
            } catch (HttpResponseException e) {
                throw new IllegalStateException('failed to download msys2 archive', e)
            }
            if (!verifyMsys2Archive(msys2ArchiveFile, TEST_MSYS2_ARCHIVE_HASH)) {
                throw new IllegalStateException('failed to verify msys2 archive')
            }
        }

        if(windows) {
            if (!download || !_testBashFile.file) {
                new File(testCacheDir, 'msys32').deleteDir()
                new TarArchiveInputStream(new XZCompressorInputStream(new BufferedInputStream(msys2ArchiveFile.newInputStream()))).withStream {
                    TarArchiveEntry entry
                    while ((entry = it.nextTarEntry) != null) {
                        final dest = new File(testCacheDir, entry.name)
                        if (entry.directory) {
                            dest.mkdirs()
                        } else {
                            dest.delete()
                            dest.parentFile.mkdirs()
                            dest.withOutputStream { outputStream ->
                                def len = 0
                                final buffer = new byte[1024]
                                while ((len = it.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, len)
                                }
                            }
                        }
                    }
                }
            }
        }

        server = HttpServer.create(new InetSocketAddress(8080), 0)
        server.createContext("/dist/$TEST_MSYS2_ARCHIVE_NAME", new HttpHandler() {
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
            apply from: file('../../../tmp/${GROUP_ID}/${ARTIFACT_ID}/integration-test-deps.gradle'), to: buildscript
        }

        apply plugin: 'com.github.jishida.shellscript'

        shellscript {
            msys2 {
                sha256 = '${TEST_MSYS2_ARCHIVE_HASH}'
                distUrl = '${TEST_MSYS2_DIST_URL}'
                cacheDir = file('cache dir')
            }
        }
        """.stripIndent()

        if (!windows) {
            buildFile << """
            // add windows tasks if not windows
            import ${Msys2Setup.canonicalName}
            task ${Msys2Setup.TASK_NAME}(type: ${Msys2Setup.simpleName})
            """.stripIndent()
        }
    }

    def cleanup() {
        projectDir.deleteDir()
    }

    private static String getJarVersion() {
        final props = new Properties()
        new File('gradle.properties').withInputStream {
            props.load(it)
        }
        props.get('version')
    }
}