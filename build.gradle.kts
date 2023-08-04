import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Janitor
import org.codehaus.groovy.control.io.FileReaderSource
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.21"
}

group = "me.mucloud"
version = fetchInfo(4)
var internalVersion = fetchInfo(6)

fun fetchInfo(line: Int): String{
    return FileReaderSource(File("src/main/resources/plugin.yml"), CompilerConfiguration.DEFAULT).getLine(line, Janitor()).split(":")[1].replace('"', ' ').trim()
}

repositories {
    mavenLocal()
    mavenCentral()

    maven("https://jitpack.io")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")

    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.3")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.withType<Jar>{
    println("""
        | The Custom Build Version Fetcher for XY-RandomSell, Running...
        | Current Version Info:
        | Version: ${archiveVersion.get()}
        | Internal Version: $internalVersion
    """.trimIndent())
    archiveFileName.set("XY-RandomSell_${archiveVersion.get()}_$internalVersion.jar")
}
