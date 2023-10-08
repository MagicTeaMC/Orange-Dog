group = "tw.maoyue"
version = "1.0.10-SNAPSHOT"
description = "ODBot"
java.sourceCompatibility = JavaVersion.VERSION_1_8

plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenLocal()
    jcenter()
    mavenCentral()
    maven {
        url = uri("https://m2.dv8tion.net/releases")
    }
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    api("net.dv8tion:JDA:4.4.1_353")
    api("dev.arbjerg:lavaplayer:2.0.2")
    api("com.jagrosh:jda-utilities:3.0.5")
    api("com.github.MagicTeaMC:MaoLyrics:8f6ee88")
    api("ch.qos.logback:logback-classic:1.4.11")
    api("com.typesafe:config:1.4.2")
    api("org.jsoup:jsoup:1.16.1")
    api("com.squareup.okhttp3:okhttp:4.11.0")
    api("com.google.code.gson:gson:2.10.1")
    api("org.slf4j:slf4j-nop:2.0.9")
    api("org.slf4j:slf4j-api:2.0.9")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.hamcrest:hamcrest-core:2.2")
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}

tasks.jar {
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