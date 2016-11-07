package com.github.jishida.gradle.shellscript.archive

import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.internal.file.archive.compression.CompressedReadableResource
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.internal.resources.URIBuilder
import org.gradle.api.resources.internal.ReadableResourceInternal
import org.tukaani.xz.XZInputStream

class TarXZUnarchiver implements Unarchiver {
    private static class XZArchiver implements CompressedReadableResource {
        private final ReadableResourceInternal _resource
        private final URI _uri

        XZArchiver(final ReadableResourceInternal resource) {
            _resource = resource;
            _uri = new URIBuilder(resource.URI).schemePrefix('xz:').build();
        }

        @Override
        InputStream read() {
            new XZInputStream(_resource.read())
        }

        @Override
        String getDisplayName() {
            _resource.getDisplayName();
        }

        @Override
        URI getURI() {
            _uri;
        }

        @Override
        String getBaseName() {
            _resource.baseName
        }

        @Override
        File getBackingFile() {
            _resource.backingFile
        }
    }

    @Override
    FileTree getFileTree(final Project project, final File archiveFile) {
        final def resource = ((DefaultProject) project).fileResolver.resolveResource(archiveFile)
        project.tarTree(new XZArchiver(resource))
    }
}