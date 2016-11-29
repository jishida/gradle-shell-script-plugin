package com.github.jishida.gradle.shellscript;

import com.github.jishida.gradle.commons.archive.*;
import com.github.jishida.gradle.commons.util.Checker;
import com.github.jishida.gradle.commons.util.MapBuilder;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static com.github.jishida.gradle.shellscript.ShellScriptStrings.*;

public class Msys2Spec {
    private final Map<String, Class<? extends Unarchiver>> unarchiverMap = new MapBuilder<String, Class<? extends Unarchiver>>(HashMap.class)
            .put("tar", TarUnarchiver.class)
            .put("tar.bz2", TarBzip2Unarchiver.class)
            .put("tar.gz", TarGzipUnarchiver.class)
            .put("tar.xz", TarXZUnarchiver.class)
            .put("zip", ZipUnarchiver.class)
            .build();

    private final Project project;

    private Project cacheProject;

    private String distUrl = DEFAULT_MSYS2_DIST_URL;
    private String bashPath = DEFAULT_MSYS2_BASH_PATH;
    private String archiveType = "tar.xz";
    private String sha256 = null;
    private boolean verify = true;
    private boolean ignoreCertificate = false;
    private File cacheDir;
    private boolean setup = true;

    Msys2Spec(final Project project) {
        this.project = project;
        cacheDir = getDefaultCacheDir();
        cacheProject = project.getRootProject();
    }

    File getCacheDirInternal() {
        try {
            return (cacheDir == null ? getDefaultCacheDir() : cacheDir).getCanonicalFile();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private File getDefaultCacheDir() {
        return project.file(DEFAULT_MSYS2_CACHE_PATH);
    }

    public void registerUnarchiver(final String archiveType, final Class<? extends Unarchiver> unarchiverClass) {
        Checker.checkNull(archiveType, "archiveType");
        Checker.checkNull(unarchiverClass, "unarchiverClass");
        unarchiverMap.put(archiveType, unarchiverClass);
    }

    Class<? extends Unarchiver> getUnarchiverClass() {
        final String type = archiveType == null ? "tar.xz" : archiveType;
        final Class<? extends Unarchiver> result = unarchiverMap.get(type);
        if (result == null) {
            throw new UnsupportedOperationException(format("unknown archive type `%s`", type));
        }
        return result;
    }

    public Project getProject() {
        return project;
    }

    public Project getCacheProject() {
        return cacheProject;
    }

    public void setCacheProject(final Project value) {
        cacheProject = value;
    }

    public String getDistUrl() {
        return distUrl;
    }

    public void setDistUrl(final String value) {
        distUrl = value;
    }

    public String getBashPath() {
        return bashPath;
    }

    public void setBashPath(final String value) {
        bashPath = value;
    }

    public String getArchiveType() {
        return archiveType;
    }

    public void setArchiveType(final String value) {
        archiveType = value;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(final String value) {
        sha256 = value;
    }

    public boolean isVerify() {
        return verify;
    }

    public void setVerify(final boolean value) {
        verify = value;
    }

    public boolean isIgnoreCertificate() {
        return ignoreCertificate;
    }

    public void setIgnoreCertificate(final boolean value) {
        ignoreCertificate = value;
    }

    public File getCacheDir() {
        return cacheDir;
    }

    public void setCacheDir(final File value) {
        cacheDir = value;
    }

    public boolean isSetup() {
        return setup;
    }

    public void setSetup(final boolean value) {
        setup = value;
    }
}
