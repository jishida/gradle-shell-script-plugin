package com.github.jishida.gradle.shellscript;

import com.github.jishida.gradle.commons.util.MapBuilder;
import org.gradle.api.Project;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.github.jishida.gradle.commons.util.Converter.hexToBytes;
import static com.github.jishida.gradle.commons.util.IOUtils.verifyFile;
import static java.util.Collections.unmodifiableMap;

public final class ShellScriptUtils {
    private final static Map<String, String> msys2Hashes = new MapBuilder<String, String>(HashMap.class)
            .put("msys2-base-i686-20150916.tar.xz", "5fae3f1c1bb3226c3a7fb7dc0ea7e8f6b4f020737bdebc62f57694433195214f")
            .put("msys2-base-i686-20160205.tar.xz", "aa18d88e10a278237ad0e421c3375be7c03249f8dd0606e69df682b9605c8de1")
            .put("msys2-base-i686-20160719.tar.xz", "cefe0ef40b10947bfbd4599ac84f96849f7f6829d1c7809af4f2be68be67cd35")
            .put("msys2-base-i686-20160921.tar.xz", "41803c61a8d0f3f57484b40d41cd73aac69f9c90fb40e2a3a7e5c90e46254c87")
            .put("msys2-base-i686-20161025.tar.xz", "8bafd3d52f5a51528a8671c1cae5591b36086d6ea5b1e76e17e390965cf6768f")
            .put("msys2-base-x86_64-20150916.tar.xz", "1eac63be7bfff979846dfda0aef6e2b572bc526450586de6364b08aaf642b843")
            .put("msys2-base-x86_64-20160205.tar.xz", "7e97e2af042e1b6f62cf0298fe84839014ef3d4a3e7825cffc6931c66cc0fc20")
            .put("msys2-base-x86_64-20160719.tar.xz", "a3255ebba5888c3b4de7a01b6febce9336c66128953f061f7d80e1d8c56582ca")
            .put("msys2-base-x86_64-20160921.tar.xz", "4527d71caf97b42e7f2c0c3d7fd80bacd36c2efc60ab81142ae9943ce3470e31")
            .put("msys2-base-x86_64-20161025.tar.xz", "bb1f1a0b35b3d96bf9c15092da8ce969a84a134f7b08811292fbc9d84d48c65d")
            .build();

    public static boolean verifyMsys2Archive(final File file, final String configuredHash) {
        final String expected = configuredHash == null ? msys2Hashes.get(file.getName()) : configuredHash;
        return expected != null && verifyFile(file, hexToBytes(expected), "SHA-256");
    }

    public static ShellScriptExtension getShellScriptExtension(final Project project) {
        return project.getExtensions().findByType(ShellScriptExtension.class);
    }

    public static Msys2Spec getMsys2Extension(final Project project) {
        final ShellScriptExtension extension = getShellScriptExtension(project);
        return extension == null ? null : extension.getMsys2();
    }

    static Msys2CacheInfo getCacheInfo(final Project project) {
        final Msys2Spec msys2 = getMsys2Extension(project);
        final Project cacheProject = msys2 == null ? project.getRootProject() : msys2.getCacheProject();
        if (getMsys2Extension(cacheProject) == null) {
            Map<String, Object> options = new MapBuilder<String, Object>(HashMap.class)
                    .put("plugin", ShellScriptPlugin.class)
                    .build();
            cacheProject.apply(options);
            getShellScriptExtension(cacheProject).getMsys2().setCacheProject(cacheProject);
        }
        return cacheProject == project ? null : getShellScriptExtension(cacheProject).configure().getMsys2().getCache();
    }
}
