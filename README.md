# 五运六气 · 运气日历（Android）

包名 `online.puwa.wuyun`。离线优先，不申请任何权限（包括 INTERNET），无后端、无统计 SDK。

- `:core` —— 纯 Kotlin/JVM 模块，不依赖 Android SDK，可在 JVM 上直接跑单元测试。
  - `WuyunCalc`：`reference/wuyun-calc.js` 的逐行 Kotlin 移植（`compute` / `stepsOf` / `branchOf` / `parseDateParam`），函数与字段名保持一致。
  - `WuyunData`：用 kotlinx.serialization 解析 `assets/yunqi-60.json` 与 `assets/cards.json`，提供 `cardFor(branch, step)`。
  - `TodayPresenter.build(instant, data, selectedStep)`：今日页状态 `TodayState`（headline / yearLabel / rangeLine / card / timeline / shareText），界面要显示的字符串全部在此拼好，Compose 只负责画。
  - `ShareImages`：分享图（1080×1440 PNG）的布局与自动换行逻辑（只换行不截字），平台渲染器实现为 `ShareImageRenderer`（测试用 AWT，app 用 Canvas）。
  - 固定中文格式串统一放在 `core/src/main/resources/wuyun-text.json`，Kotlin/XML 源码中不出现中文字面量（见验收 D）。
- `:app` —— Jetpack Compose 工程（minSdk 26，targetSdk 37）。今日卡、六步时间轴、原文展开、说明弹窗、分享（文字 / 1080×1440 分享图 + 二维码，FileProvider 走 cache 目录）。主色按选中步客气取色（映射在 `assets/qi-colors.json`），支持深色模式。界面固定文案全部在 `res/values/strings.xml`。

P1 调试注入日期（仅 debug 版）：

```bash
adb shell am start -n online.puwa.wuyun/.MainActivity --es date 2028-01-21T12:00
```

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

`local.properties` 不随包提交（已 gitignore），请在本机创建并指向你的 SDK：

```
sdk.dir=/path/to/Android/Sdk        # Windows 示例：sdk.dir=D:/devtools/android-sdk
```

```bash
./gradlew :core:test        # 推算 + 数据层 + Presenter + 分享图单元测试（对拍 reference/golden.json）
./gradlew assembleDebug     # 打包 debug APK
./gradlew lint              # Android Lint
```

测试资源直接复用仓库里的 `reference/golden.json` 与 `app/src/main/assets/*.json`（见 `core/build.gradle.kts` 的 test sourceSet 配置），保证测的就是打进 APK 的同一份数据。

> 网络提示：`gradle/wrapper/gradle-wrapper.properties` 的 distributionUrl 是官方地址
> `https://services.gradle.org/distributions/gradle-9.9.1-bin.zip`；
> `settings.gradle.kts` 中在 google()/mavenCentral()/gradlePluginPortal() 之后配置了阿里云镜像作为回退，
> 直连官方仓库失败时会自动走镜像。

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
