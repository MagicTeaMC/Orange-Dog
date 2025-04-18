group = "tw.maoyue"
version = "1.2.1"
description = "My Discord music bot, base on JMusicBot"

plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("jvm")
}

repositories {
    mavenLocal()
    jcenter()
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
    maven {
        url = uri("https://nexus.scarsz.me/content/repositories/releases/")
    }
    maven {
        url = uri("https://m2.chew.pro/snapshots")
    }
    maven {
        url = uri("https://maven.lavalink.dev/releases")
    }
    maven {
        url = uri("https://maven.topi.wtf/releases")
    }
}

dependencies {
    api("net.dv8tion:JDA:5.3.2")
    api("dev.arbjerg:lavaplayer:2.2.3")
    api("dev.lavalink.youtube:v2:1.12.0")
    api("pw.chew:jda-chewtils:2.1-SNAPSHOT")
    api("com.github.MagicTeaMC:MaoLyrics:b74346f")
    api("ch.qos.logback:logback-classic:1.5.17")
    api("com.typesafe:config:1.4.3")
    api("org.jsoup:jsoup:1.18.3")
    api("com.squareup.okhttp3:okhttp:4.12.0")
    api("com.google.code.gson:gson:2.11.0")
    api("com.github.topi314.lavasrc:lavasrc:4.6.0")
    api("com.github.topi314.lavasrc:lavasrc-protocol:4.6.0")
    api("com.github.topi314.lavalyrics:lavalyrics:1.0.0")
    api("org.json:json:20250107")
    api("org.slf4j:slf4j-nop:2.0.16")
    api("org.slf4j:slf4j-api:2.0.16")
    api("me.scarsz.jdaappender:jda5:1.2.3")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.hamcrest:hamcrest-core:3.0")
    implementation(kotlin("stdlib-jdk8"))
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.shadowJar {
    archiveFileName = "OrangeDog-$version.jar"
}

tasks.jar {
    archiveFileName = "originalOrangeDog-$version.jar"
    manifest {
        attributes(
                "Main-Class" to "com.jagrosh.jmusicbot.JMusicBot",
                "Specification-Title" to project.name,
                "Specification-Version" to project.version,
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Implementation-Vendor-Id" to project.group
        )
    }
}
kotlin {
    jvmToolchain(11)
}