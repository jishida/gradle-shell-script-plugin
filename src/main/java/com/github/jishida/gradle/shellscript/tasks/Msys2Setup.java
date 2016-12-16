package com.github.jishida.gradle.shellscript.tasks;

import com.github.jishida.gradle.commons.function.Proc;
import com.github.jishida.gradle.commons.util.IOUtils;
import com.github.jishida.gradle.commons.util.MapBuilder;
import com.github.jishida.gradle.shellscript.Msys2CacheInfo;
import com.github.jishida.gradle.shellscript.Msys2Info;
import groovy.json.JsonOutput;
import groovy.json.JsonSlurper;
import org.gradle.api.Action;
import org.gradle.api.file.CopySpec;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecSpec;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.github.jishida.gradle.commons.util.IOUtils.deleteFile;
import static com.github.jishida.gradle.commons.util.NetUtils.downloadFile;
import static com.github.jishida.gradle.shellscript.ShellScriptUtils.verifyMsys2Archive;
import static java.lang.String.format;

public class Msys2Setup extends AbstractShellScriptTask {
    public final static String TASK_NAME = "msys2Setup";

    @Input
    public Map<String, Object> getSetupStatus() {
        final Msys2Info info = getMsys2Info();
        if (info == null) return null;
        final Msys2CacheInfo cache = info.getCache();
        final MapBuilder<String, Object> builder = new MapBuilder<String, Object>(HashMap.class)
                .put("setup", info.isSetup())
                .put("bashFile", cache.getBashFile().getPath());
        if (info.isSetup()) {
            builder.put("distUrl", cache.getDistUrl())
                    .put("unarchiverClass", cache.getUnarchiverClass().getCanonicalName())
                    .put("verify", cache.isVerify())
                    .put("ignoreCertificate", cache.isIgnoreCertificate());
            if (cache.getExpandDirName() != null) {
                builder.put("expandDirName", cache.getExpandDirName());
            }
            if (cache.isVerify() && cache.getHash() != null) {
                builder.put("hash", cache.getHash());
            }
        }
        return builder.build();
    }

    @OutputFile
    public File getSetupStatusFile() {
        final Msys2Info info = getMsys2Info();
        return info == null ? null : info.getCache().getSetupStatusFile();
    }

    @OutputFile
    public File getBashFile() {
        final Msys2Info info = getMsys2Info();
        return info == null ? null : info.getCache().getBashFile();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readStatus() {
        final Msys2CacheInfo cache = getMsys2Info().getCache();
        final JsonSlurper json = new JsonSlurper();
        try {
            return (Map<String, Object>) json.parse(cache.getSetupStatusFile());
        } catch (Exception e) {
            return null;
        }
    }

    private void writeStatus(final Map<String, Object> status) {
        final Msys2CacheInfo cache = getMsys2Info().getCache();
        try {
            IOUtils.withWriter(cache.getSetupStatusFile(), false, new Proc<Writer>() {
                @Override
                public void invoke(Writer it) {
                    try {
                        it.write(JsonOutput.prettyPrint(JsonOutput.toJson(status)));
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @TaskAction
    public void setup() throws IOException {
        if (!getMsys2Info().hasCache()) return;
        final Msys2CacheInfo cache = getMsys2Info().getCache();

        if(getMsys2Info().isSetup()) {
            final Map<String, Object> status = getSetupStatus();
            final Map<String, Object> previousStatus = readStatus();
            writeStatus(status);

            if (previousStatus == null || !status.equals(previousStatus)) {
                final File archiveFile = cache.getArchiveFile();
                if (!archiveFile.isFile() || (cache.isVerify() && !verifyMsys2Archive(archiveFile, cache.getHash()))) {
                    deleteFile(archiveFile);
                    final File archiveDir = archiveFile.getParentFile();
                    if (!archiveDir.mkdirs() && archiveDir.isFile()) {
                        throw new IllegalStateException(format("`%s` is not directory.", archiveDir));
                    }
                    downloadFile(cache.getDistUrl(), archiveFile, cache.isIgnoreCertificate());

                    if (cache.isVerify() && !verifyMsys2Archive(archiveFile, cache.getHash())) {
                        throw new IllegalStateException(format("failed to verify `%s`.", archiveFile));
                    }
                }

                final File cacheDir = cache.getCacheDir();
                if (previousStatus != null) {
                    final Object dirName = previousStatus.get("expandDirName");
                    if (dirName != null) {
                        final File expandDir = new File(cacheDir, dirName.toString());
                        final File parent = expandDir.getParentFile();
                        if (parent != null && cacheDir.equals(parent)) {
                            deleteFile(expandDir);
                        }
                    }
                }

                if (!cacheDir.mkdirs() && cacheDir.isFile()) {
                    throw new IllegalStateException(format("`%s` is not directory.", cacheDir));
                }

                getProject().copy(new Action<CopySpec>() {
                    @Override
                    public void execute(CopySpec it) {
                        it.from(cache.unarchive());
                        it.into(cacheDir);
                    }
                });
            }
        }

        getProject().exec(new Action<ExecSpec>() {
            @Override
            public void execute(final ExecSpec it) {
                it.setExecutable(cache.getBashFile());
                it.setArgs(Arrays.asList("--login", "-c", "exit 0"));
            }
        }).rethrowFailure();
    }
}
