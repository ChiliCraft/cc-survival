// cc-survival：生存压力附属：饥饿加速 / 体温 / 负重 / 耐久损耗
plugins {
    id("java-library")
}

group = "com.chilicraft"
version = "1.0.0"

// 统一工具链：Java 21 编译。不用 JDK 17 的原因见 monorepo 根构建脚本注释：
// 中文 Windows（GBK）下 JDK 17 worker 按平台编码读 UTF-8 argfile 会崩，JDK 18+ 默认 UTF-8。
java {
    toolchain { languageVersion = JavaLanguageVersion.of(21) }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 21
    options.compilerArgs.add("-Xlint:deprecation")
}

tasks.withType<Javadoc>().configureEach {
    options.encoding = "UTF-8"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") { name = "papermc" }
    // cc-core：先查本地发布（./gradlew -p ../cc-core publishToMavenLocal），再查 GitHub Packages
    mavenLocal()
    maven {
        url = uri("https://maven.pkg.github.com/ChiliCraft/cc-core")
        credentials {
            username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
            password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    // 核心 API：运行时由服务端上已安装的 cc-core 提供，不打进 jar
    compileOnly("com.chilicraft:cc-core:1.0.0")
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("org.slf4j:slf4j-api:2.0.13")
}

// 把版本号注入 plugin.yml（${version} 占位）
tasks.processResources {
    val props = mapOf("version" to project.version.toString())
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}
