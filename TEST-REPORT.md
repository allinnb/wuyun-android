# P0 验收测试报告

环境：JDK Temurin 21.0.13、Gradle 9.9.1（wrapper）、AGP 9.5.1、Kotlin 2.4.21、compileSdk/targetSdk 37（platforms;android-37.2）、build-tools 37.0.0、**cn.6tail:lunar 1.7.8**（与网页版 lunar-javascript 1.7.8 同版号）。

命令：`./gradlew :core:test --rerun-tasks`、`./gradlew :core:test assembleDebug` —— 均 `BUILD SUCCESSFUL`，7/7 测试 PASSED。

| 验收项 | 内容 | 结果 |
|---|---|---|
| A | golden.json 全部 1554 个 cases，逐字段（含 headline、精确到秒的时刻）比对 compute 结果 + branchOf | **1554/1554 通过** |
| B | stepsOf(2026..2087) 与 golden.stepsOf 逐年逐步（step/stepName/from/fromTime/to/toTime）比对 | **62/62 年（372 步）全部一致** |
| C1 | 6 个 branch × 6 步共 36 组都能 cardFor 取到卡 | **36/36** |
| C2 | 每个 golden case 用 branch+step 取卡，卡的 zhuqi/keqi 与推算结果一致 | **1554/1554 通过** |
| D | 抽查 2028-01-21 12:00 → headline「大寒 · 第 2 天｜初之气（大寒—春分），戊申」，主气 厥阴风木，客气 少阴君火 | **通过**（另有 jieqi=大寒、step=1、yunqiYear=2028、ganzhi=戊申、branch=寅申 断言） |
| E | `./gradlew :core:test` 与 `./gradlew assembleDebug` | **均 BUILD SUCCESSFUL**（APK: app/build/outputs/apk/debug/app-debug.apk，内含 assets/cards.json、assets/yunqi-60.json） |

补充测试：`parseDateParam`（日期-only 取当天 12:00 北京时间、`YYYY-MM-DDTHH:mm`、非法格式返回 null、0 点前回绕上一日 UTC）、`branchOf` 十二地支映射 —— 均通过。

## 过程中遇到的问题（均已修复，未改动 golden.json，无任何特例）

1. `java.util.Date.UTC` 的年份参数语义是 year-1900（与 JS 版 Date.UTC 不同），移植时多减了 1900 导致年份偏移 1900 年 —— 已改为 `Date.UTC(y - 1900, ...)`，与 JS 版逐毫秒一致。
2. 测试名 `D spot check 2028-01-21 12:00` 含冒号在 Kotlin 反引号标识符中非法 —— 测试名改为 `12-00`。
3. lunar-java 与 lunar-javascript 均为 1.7.8，`getJieQiTable()` 同时包含中文键与拼音键（如 `DA_HAN` = 次年大寒），`getPrevJieQi(true)` 语义一致 —— 未发现版本差异，1554 个用例一次全过。
