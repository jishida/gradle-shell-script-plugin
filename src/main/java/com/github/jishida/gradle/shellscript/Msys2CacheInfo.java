package com.github.jishida.gradle.shellscript;

import com.github.jishida.gradle.commons.archive.Unarchiver;
import org.gradle.api.Project;
import org.gradle.api.file.FileTree;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static com.github.jishida.gradle.commons.util.IOUtils.getAbsoluteFile;
import static com.github.jishida.gradle.commons.util.NetUtils.findFileName;
import static com.github.jishida.gradle.shellscript.ShellScriptStrings.*;
import static java.lang.String.format;

public class Msys2CacheInfo {
    private final Unarchiver unarchiver;

    private final Project project;

    private final URL distUrl;
    private final File cacheDir;
    private final File bashFile;
    private final File archiveFile;
    private final String expandDirName;
    private final File setupStatusFile;
    private final Class<? extends Unarchiver> unarchiverClass;
    private final String hash;
    private final boolean verify;
    private final boolean ignoreCertificate;

    Msys2CacheInfo(final Msys2Spec spec) {
        project = spec.getProject();
        try {
            distUrl = new URL(spec.getDistUrl() == null ? DEFAULT_MSYS2_DIST_URL : spec.getDistUrl());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
        cacheDir = spec.getCacheDirInternal();
        bashFile = getAbsoluteFile(spec.getBashPath() == null ? DEFAULT_MSYS2_BASH_PATH : spec.getBashPath(), cacheDir);
        archiveFile = new File(project.getBuildDir(), format("%s/%s", TEMP_PATH, findFileName(distUrl, "msys2_archive")));
        expandDirName = findExpandDirName(cacheDir, bashFile, true);
        setupStatusFile = new File(cacheDir, "setup.json");
        unarchiverClass = spec.getUnarchiverClass();
        hash = spec.getSha256();
        verify = spec.isVerify();
        ignoreCertificate = spec.isIgnoreCertificate();
        try {
            unarchiver = unarchiverClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Project getProject() {
        return project;
    }

    public URL getDistUrl() {
        return distUrl;
    }

    public File getCacheDir() {
        return cacheDir;
    }

    public File getBashFile() {
        return bashFile;
    }

    public File getArchiveFile() {
        return archiveFile;
    }

    public String getExpandDirName() {
        return expandDirName;
    }

    public File getSetupStatusFile() {
        return setupStatusFile;
    }

    public Class<? extends Unarchiver> getUnarchiverClass() {
        return unarchiverClass;
    }

    public String getHash() {
        return hash;
    }

    public boolean isVerify() {
        return verify;
    }

    public boolean isIgnoreCertificate() {
        return ignoreCertificate;
    }

    public FileTree unarchive() {
        return unarchiver.getFileTree(project, archiveFile);
    }

    private static String findExpandDirName(final File base, final File target, final boolean isFile) {
        final File parent = target.getParentFile();
        if (parent == null) return null;
        if (base.equals(parent)) {
            return isFile ? null : target.getName();
        } else {
            return findExpandDirName(base, parent, false);
        }
    }
}
