plugins {
    java
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.mediasteru"
version = rootProject.version

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(project(":core"))
    
    // UI dependencies
    implementation("com.formdev:flatlaf:3.7.2")
    implementation("com.google.code.gson:gson:2.10.1")
    
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    mainClass.set("com.mediasteru.desktop.Program")
    archiveBaseName.set("MediaSteru")
    archiveClassifier.set("")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
