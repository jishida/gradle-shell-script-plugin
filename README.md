gradle-shell-script-plugin
==========================

This plugin enables you to run shell scripts on multi-platforms. If you run a
shell script task on Windows, the task will install MSYS2 to the cache
directory of your project and run scripts on the MSYS2 environment.

## Usage

```gradle
buildscript {
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/"
        }
    }
    
    dependencies {
        classpath group: 'com.github.jishida.gradle', name: 'gradle-shell-script-plugin', version: '0.1'
    }
}

apply plugin: 'com.github.jishida.shellscript'

task myShellScriptTask(type: ShellScript) {
    scriptFile = file('my-script.sh')
}
```

## Options

```gradle
// project settings
shellscript {
    // shell command when running on UNIX platfroms
    // type: java.lang.String
    // default: 'bash'
    unixShell = '/bin/usr/env'
    
    // command line options of shell command
    // type: java.util.List<java.lang.String>
    // default: null
    shellArgs = ['bash', '--verbose',]
    
    // Windows platform settings
    msys2 {
        // a project whose settings are referenced
        // type: org.gradle.api.Project
        // default: rootProject
        cacheProject = project(':other-project')
        
        // distribution URL of MSYS2
        // type: java.lang.String
        // default: 'http://repo.msys2.org/distrib/i686/msys2-base-i686-20160921.tar.xz'
        distUrl = 'http://localhost:8080/dist/msys2-base-x86_64-20160921.tar.xz'
        
        // bash binary path from expanded directory
        // type: java.lang.String
        // default: 'msys32/usr/bin/bash.exe'
        bashPath = 'msys64/usr/bin/bash.exe'
        
        // MSYS2 archive file type ('tar', 'tar.bz2', 'tar.gz', 'tar.xz' or 'zip')
        // type: java.lang.String
        // default: 'tar.xz'
        archiveType = 'tar.xz'
        
        // enable to verify MSYS2 archive file
        // type: boolean
        // default: true
        verify = true
        
        // SHA-256 hash of MSYS2 archive file
        // type: java.lang.String
        // default: null (use pre-computed hashes)
        sha256 = '4527d71caf97b42e7f2c0c3d7fd80bacd36c2efc60ab81142ae9943ce3470e31'
        
        // MSYS2 cache directory
        // type: java.io.File
        // default: file('.gradle/msys2')
        cacheDir = file('.msys2')
        
        // enable to download and install MSYS2
        // type: boolean
        // default: true
        setup = true
    }
}

// define shell script task
task sampleTask(type: ShellScript) {
    // a directory running shell script
    // type: java.io.File
    // default: null
    workingDir = file('working_dir')
    
    // command line options of shell command
    // type: java.util.List<java.lang.String>
    // default: null
    args = ['-c', 'ls -a ../files > filelist.txt']
    
    // MSYS2 option (MSYS, MINGW32 or MINGW64)
    // type: java.lang.String
    // default: 'MSYS'
    mSystem = 'MINGW64'
    
    // shell script file
    // type: java.io.File
    // default: null
    scriptFile = null
    
    // TaskInputs and TaskOutputs
    inputs.dir file('files')
    outputs.file file('working_dir/filelist.txt')
}
```