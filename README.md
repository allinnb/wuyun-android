# 五运六气 · 运气日历（Android P0 地基）

包名 `online.puwa.wuyun`。本阶段（P0）只做推算模块与数据加载，不做界面。

- `:core` —— 纯 Kotlin/JVM 模块，不依赖 Android SDK，可在 JVM 上直接跑单元测试。
  - `WuyunCalc`：`reference/wuyun-calc.js` 的逐行 Kotlin 移植（`compute` / `stepsOf` / `branchOf` / `parseDateParam`），函数与字段名保持一致。
  - `WuyunData`：用 kotlinx.serialization 解析 `assets/yunqi-60.json` 与 `assets/cards.json`，提供 `cardFor(branch, step)`。
- `:app` —— 空的 Jetpack Compose 工程（minSdk 26，targetSdk 37），`WuyunRepository` 从 assets 加载数据。

## 关键依赖版本

| 依赖 | 版本 | 说明 |
|---|---|---|
| Gradle | 9.9.1 | wrapper，见 `gradle/wrapper/gradle-wrapper.properties` |
| AGP | 9.5.1 | 内置 Kotlin 支持（AGP 9 起不再需要 kotlin.android 插件） |
| Kotlin | 2.4.21 |含 compose / serialization 插件 |
| compileSdk / targetSdk | 37（当前稳定版，平台包 `platforms;android-37.2`） | minSdk 26 |
| **cn.6tail:lunar** | **1.7.8** | lunar-java，与网页版 lunar-javascript **1.7.8** 同源同版本号，节气时刻逐秒一致 |
| kotlinx-serialization-json | 1.13.0 | |
| JUnit | 4.13.2 | |

所有时刻按 `Asia/Shanghai`（北京时间）计算，不依赖设备时区；时间字符串格式 `yyyy-MM-dd HH:mm:ss`。

## 构建与测试

需要 JDK 17+（推荐 21）和 Android SDK（platform 37、build-tools 37.0.0）。
`local.properties` 中指定 `sdk.dir`（本仓库已按本机路径配置，其他机器请改成自己的 SDK 路径）。

```bash
./gradlew :core:test        # 推算 + 数据层单元测试（对拍 reference/golden.json）
./gradlew assembleDebug     # 打包 debug APK
```

测试资源直接复用仓库里的 `reference/golden.json` 与 `app/src/main/assets/*.json`（见 `core/build.gradle.kts` 的 test sourceSet 配置），保证测的就是打进 APK 的同一份数据。

> 网络提示：`settings.gradle.kts` 中在 google()/mavenCentral() 之后配置了阿里云镜像作为回退；
> `gradle-wrapper.properties` 的 distributionUrl 指向腾讯 Gradle 镜像（国内直连更快），可自行改回
> `https://services.gradle.org/distributions/gradle-9.9.1-bin.zip`。

## 验收结果

见 [TEST-REPORT.md](TEST-REPORT.md)：A 1554/1554，B 62/62 年，C 36/36 卡且 1554/1554 用例卡气一致，D 抽查通过，E 两条命令构建成功。

## 目录结构

```
wuyun-android/
├── app/                        # Compose 空壳 + assets 数据加载
│   └── src/main/assets/        # yunqi-60.json, cards.json（文案只来自这里，代码不写死文案）
├── core/                       # 纯 JVM 推算模块
│   └── src/main/java/online/puwa/wuyun/core/
│       ├── WuyunCalc.kt        # wuyun-calc.js 移植
│       ├── WuyunData.kt        # JSON 数据层，cardFor(branch, step)
│       └── Models.kt           # @Serializable 模型
│   └── src/test/...            # GoldenTest（验收 A-D）
├── reference/                  # 工具包原件：golden.json、wuyun-calc.js、ANDROID-PLAN.md、STYLE.md
└── gradle/libs.versions.toml   # 版本号集中管理
```

## 规矩（来自 ANDROID-PLAN.md / STYLE.md）

产品定位为传统文化和养生资讯：不做诊断，不写功效，不添加任何健康类文案；卡面文案全部来自 JSON，一个字都不改、不写进代码。
