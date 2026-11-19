# P1 验收测试报告

环境：JDK Temurin 21.0.13、Gradle 9.9.1（wrapper，distributionUrl 为官方 services.gradle.org）、AGP 9.5.1、Kotlin 2.4.21、compileSdk/targetSdk 37（platforms;android-37.2）、build-tools 37.0.0、**cn.6tail:lunar 1.7.8**、kotlinx-serialization-json 1.13.0、zxing core 3.5.4、JUnit 4.13.2。模拟器：android-37.0 google_apis x86_64。

| 验收项 | 内容 | 结果 |
|---|---|---|
| A | P0 的 7 个 golden 测试（1554 cases / stepsOf / cards / 抽查 / parseDateParam / branchOf）回归 | **7/7 PASSED**（`:core:test` 共 12 个测试方法全过） |
| B | TodayPresenter 表格：6 组 input（parseDateParam 转换）比对 card.title 与 rangeLine 区间（含括号内日期）；selectedStep=3 时 headline 不变、主客少阳相火 | **6/6 通过** |
| C | golden 1554 个 input × selectedStep(null,1..6) = 10878 个状态：card 非空、9 个字段非空、timeline 恰 6 项且 title 非空、无「null/undefined/准备中/待补」 | **10878/10878 通过** |
| D | 全仓库 Kotlin/XML（strings.xml、测试代码、注释除外）中文字面量扫描 | **0 违规**（扫描 18 个源文件；固定中文文案迁到 `core/.../wuyun-text.json` 与 `strings.xml`/`assets/qi-colors.json`） |
| E | `./gradlew :core:test`、`./gradlew assembleDebug`、`./gradlew lint` | **3/3 BUILD SUCCESSFUL**（lint 0 error 0 warning） |
| F | 模拟器截图：浅色/深色 × 2028-01-21T12:00 与 2026-11-08T12:00（共 4 张首屏）+ 原文展开 1 张 | **5/5 已截**，存 `reference/screens/`（见下方说明） |

分享图：`:core` 单元测试用 AWT 渲染器对 36 张卡各生成 1080×1440 PNG，全部成功（PNG magic 校验 + 大小校验）；样图 3 张在 `reference/share-samples/`（标题最长 / 最短 / 寒水蓝）。换行测试确认长标题只换行不截字。

## F 说明（模拟器环境限制）

本机无硬件虚拟化（WHPX 不可用），模拟器用 swiftshader 软件渲染；高分辨率下 SystemUI 会 ANR，因此截图分辨率为 320×480（像素级首屏，内容完整，浅/深色背景经像素校验可区分）。每次拍摄前重启模拟器保证 SystemUI 健康（注意该模拟器上 `ui_night_mode` 1=深色、2=浅色）。「原文展开」通过 uiautomator dump 滚动定位「原文」节点中心后 `input tap`，二次 dump 确认「人话」出现（count=1）。文件：`2028-01-21-light.png` / `2028-01-21-dark.png` / `2026-11-08-light.png` / `2026-11-08-dark.png` / `2028-01-21-classic-expanded.png`。

## P0 遗留修复

- 全部文件 LF（`.gitattributes`: `* text=auto eol=lf`，`*.bat text eol=crlf`），`gradlew` 在 git 中为 100755；blob 经 `git show` 验证无 CR。
- README 已改为实际描述：wrapper distributionUrl = 官方 services.gradle.org；`local.properties` 不随包，需本地创建 `sdk.dir`。
- 已推送 GitHub `allinnb/wuyun-android` 的 main 分支（gh 登录 allinnb 账号创建仓库后 push）。
