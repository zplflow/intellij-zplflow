plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.1.0"
}

group = "com.zplflow"
version = "0.2.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        local("/home/valerio/Scaricati/GoLand-2026.3")
        bundledPlugin("com.intellij.modules.jcef")
        instrumentationTools()
    }
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xskip-metadata-version-check")
    }
}

tasks.test {
    enabled = false
}

tasks.named("buildSearchableOptions") {
    enabled = false
}

tasks.prepareSandbox {
    from("bin") {
        into("${pluginName.get()}/bin")
    }
}
