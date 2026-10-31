plugins {
    base
    id("org.springframework.boot") version "3.2.3" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
}

group = "com.mediasteru"
version = "1.0.0"

allprojects {
    repositories {
        mavenCentral()
    }
}

// Copy dependency requirement from 'tools/' into build/libs/tools
val copyToolsTask = tasks.register<Copy>("copyTools") {
    from("tools")
    into(layout.buildDirectory.dir("libs/tools"))
}

// Package distribution for release
val packageReleaseTask = tasks.register<Zip>("packageRelease") {
    dependsOn(":desktop:bootJar")
    
    archiveBaseName.set("MediaSteru")
    archiveVersion.set(project.version.toString())
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    
    from(project(":desktop").tasks.named("bootJar"))
    from("tools") {
        into("tools")
    }
}

// Delegate bootJar task to desktop subproject for CI / CLI compatibility
tasks.register("bootJar") {
    dependsOn(":desktop:bootJar")
    finalizedBy(copyToolsTask)
}