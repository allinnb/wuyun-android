plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    // 节气库：lunar-java（cn.6tail:lunar），与网页版 lunar-javascript 同源
    api(libs.lunar)
    api(libs.kotlinx.serialization.json)
    // 二维码（分享图），纯 Java，Android/JVM 都能跑
    api(libs.zxing)

    testImplementation(libs.junit)
}

// 测试资源：reference/golden.json（标准答案）与 app/src/main/assets 下的两个 assets JSON
sourceSets.named("test") {
    resources.srcDir(rootProject.file("reference"))
    resources.srcDir(rootProject.file("app/src/main/assets"))
}

tasks.test {
    useJUnit()
    // 测试里用仓库根目录作为工作目录（样图写到 reference/share-samples/）
    workingDir = rootProject.projectDir
    testLogging {
        events("passed", "failed", "skipped")
    }
    // 1554 个 golden 用例，失败时输出完整对不上列表
    maxHeapSize = "1g"
}
