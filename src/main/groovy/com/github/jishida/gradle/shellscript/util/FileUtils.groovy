package com.github.jishida.gradle.shellscript.util

import org.gradle.internal.impldep.org.apache.commons.collections.map.UnmodifiableMap

import java.security.MessageDigest

final class FileUtils {
    private final static Map<String, String> msys2Hashes

    static {
        msys2Hashes = UnmodifiableMap.decorate([
                'msys2-base-i686-20150916.tar.xz'  : '5fae3f1c1bb3226c3a7fb7dc0ea7e8f6b4f020737bdebc62f57694433195214f',
                'msys2-base-i686-20160205.tar.xz'  : 'aa18d88e10a278237ad0e421c3375be7c03249f8dd0606e69df682b9605c8de1',
                'msys2-base-i686-20160719.tar.xz'  : 'cefe0ef40b10947bfbd4599ac84f96849f7f6829d1c7809af4f2be68be67cd35',
                'msys2-base-i686-20160921.tar.xz'  : '41803c61a8d0f3f57484b40d41cd73aac69f9c90fb40e2a3a7e5c90e46254c87',
                'msys2-base-i686-20161025.tar.xz'  : '8bafd3d52f5a51528a8671c1cae5591b36086d6ea5b1e76e17e390965cf6768f',
                'msys2-base-x86_64-20150916.tar.xz': '1eac63be7bfff979846dfda0aef6e2b572bc526450586de6364b08aaf642b843',
                'msys2-base-x86_64-20160205.tar.xz': '7e97e2af042e1b6f62cf0298fe84839014ef3d4a3e7825cffc6931c66cc0fc20',
                'msys2-base-x86_64-20160719.tar.xz': 'a3255ebba5888c3b4de7a01b6febce9336c66128953f061f7d80e1d8c56582ca',
                'msys2-base-x86_64-20160921.tar.xz': '4527d71caf97b42e7f2c0c3d7fd80bacd36c2efc60ab81142ae9943ce3470e31',
                'msys2-base-x86_64-20161025.tar.xz': 'bb1f1a0b35b3d96bf9c15092da8ce969a84a134f7b08811292fbc9d84d48c65d',
        ])
    }

    static void deleteFile(final File file) {
        if (file.isFile()) {
            if (!file.delete()) {
                throw new UnsupportedOperationException("cannot delete `${file}`")
            }
        } else if (file.isDirectory()) {
            if (!file.deleteDir()) {
                throw new UnsupportedOperationException("cannot delete `${file}`")
            }
        }
    }

    static boolean verifyMsys2Archive(final File file, final String configuredHash = null) {
        final expected = configuredHash ?: msys2Hashes[file.name]
        if (expected == null) return false

        try {
            file.withInputStream {
                final buffer = new byte[1024]
                final md = MessageDigest.getInstance('SHA-256')
                int size
                while ((size = it.read(buffer)) >= 0) {
                    if (size != 0) {
                        md.update(buffer, 0, size)
                    }
                }
                final hash = md.digest()
                def actual = new StringBuilder(64)
                for (b in hash) {
                    actual.append(String.format('%02x', b))
                }
                return actual.toString() == expected.toLowerCase()
            }
        } catch (Exception e) {
            false
        }
    }
}