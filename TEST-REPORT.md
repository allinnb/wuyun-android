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

# P2–P4 验收测试报告

在 P1 工程上继续（P2 时令/四气调神/十二时辰 + 交节提醒 + 桌面小组件；P3 测一测两图的分享；P4 记录/餐盘/情志日记）。环境与 P1 相同（JDK 21、Gradle 9.9.1、AGP 9.5.1、Kotlin 2.4.21、compileSdk/targetSdk 37、lunar 1.7.8、zxing 3.5.4、JUnit 4.13.2），模拟器 android-37.0 google_apis x86_64（320×720 截图，软渲染）。

| 验收项 | 内容 | 结果 |
|---|---|---|
| A | `./gradlew :core:test` 不得改算法与期望值 | **21/21 PASSED**（Golden 7、今日 2、分享图 3、Life 9；assets JSON 与 :core 算法/期望零改动，`git diff` 核对仅 ShareImagesTest.kt 按要求把样图 headline 从占位词换成 `TodayPresenter.build(parseDateParam("2028-01-21T12:00")).headline`） |
| B | D 规则：除 strings.xml 与 JSON 外，Kotlin/XML 无中文字面量（测试与注释除外） | **0 违规**（扫描 34 个源文件） |
| C | 截图 reference/screens-p2p4/：时令页、生命节律（seven，1991）、五态人结果、餐盘、情志日记（含关心提示）、小组件、通知，浅色各一张 | **8/8 已截并经 uiautomator dump 内容验证**：`season-2026-11-19.png`（冬三月 · 闭藏）、`season-2026-11-19-shichen.png`（现在是巳时（09:00–11:00））、`test-life-1991-seven.png`（35 岁 / age 35，“五七，阳明脉衰，面始焦，发始堕”，下一个节点 42 岁）、`test-wutai-result.png`（太阴之人 · 慢热的稳派 + 原文展开）、`record-plate.png`、`record-diary-care.png`（难过 3 条 → “连着好几天都很难过，找个信得过的人聊聊吧，需要的话也可以找专业的人。”）、`widget-home.png`（widget_title=干燥，还带点火；headline=立冬 · 第 13 天｜五之气（秋分—小雪），丙午；申时行）、`notification-shade.png`（标题 终之气，正文 2026-11-22 后交小雪，新的一步气要来了） |
| D | `--es date 2026-11-19T09:30` 启动，时令页显示“冬三月”、时辰为“巳” | **通过**（页首 `冬三月 · 闭藏`、十二时辰区 `现在是巳时（09:00–11:00）`，dump 双确认） |
| E | 权限：仅 POST_NOTIFICATIONS、RECEIVE_BOOT_COMPLETED（SCHEDULE_EXACT_ALARM 未用：AlarmManager 用 `setAndAllowWhileIdle`，不动精确闹钟）；无 INTERNET | **通过**（AndroidManifest 仅上述两条 uses-permission；`:core:test`/`assembleDebug`/`lint` 全绿，lint 0 error） |
| F | 推送 main + TEST-REPORT 追加本节 | **通过**（见 git log） |

## 交节提醒（P2）

- `ReminderScheduler`（`setAndAllowWhileIdle`，无需精确闹钟权限）+ `ReminderReceiver`（通知后顺排下一条）+ `BootReceiver`（开机重排）。通知渠道 `remind`，NOTIFY_ID 1001；无 POST_NOTIFICATIONS 时静默重排不弹。
- ReminderPlanner 用 :core 的 `WuyunCalc.stepsOf` 求下一个交节时刻，无自算节气。

## 桌面小组件（P2）

- `WuyunWidgetProvider` + RemoteViews（`widget_wuyun`）：title=当前步卡片标题、headline、`%1$s时 · %2$s` 时辰行；updatePeriodMillis=1800000，另有 ACTION_TICK 定时刷新（取下一奇数整点）。点击打开 MainActivity。

## P3 分享

- `Share.kt` 新增 `shareCustomImage`：生命节律结果（title=阶段中英文，share=closing，headline=about）、五态人结果（title=类型名，share=plain，headline=about）沿用 1080×1440 分享图与二维码，经 FileProvider 从 cache 分享，不写相册、不加权限。

## P2–P4 期间新增/修改的固定文案（全部在 strings.xml，未进代码）

- 新增于本地仓库（本阶段实现需要）：`tab_today 今日`、`tab_season 时令`、`tab_test 测一测`、`tab_record 记录`（随 HANDOFF 初稿一并纳入）
- 由我新增的 4 条：`ui_remind 交节提醒`、`ui_notify_channel 交节提醒`、`ui_notify_body %1$s 后交%2$s，新的一步气要来了`、`ui_widget_shichen %1$s时 · %2$s`
- HANDOFF zip 初稿已带、由我核对使用的：`ui_season_range`、`ui_sleep 作息`、`ui_todo 这季可以`、`ui_shichen 十二时辰`、`ui_shichen_now 现在是%1$s时（%2$s）`、`ui_shichen_left 配%1$s · 还有 %2$d 分钟换时辰`、`ui_life 生命节律`、`ui_wutai 五态人`、`ui_lang_zh/en`、`ui_birth_year`、`ui_age`、`ui_next_stage`、`ui_retry 再测一次 / Retake`、`ui_plate 五味餐盘`、`ui_diary 情志日记`、`ui_wuyin 五音`、`ui_diary_today 今天心情`、`ui_note 一句话（可不填）`、`ui_save 记下`
- JSON（assets 与 :core）零改动；文案零改动。

## 修复的一个隐藏 bug（编译期配置）

- 现象：真机/模拟器上点「七年节律（女子）」闪退，`kotlinx.serialization.SerializationException: Serializer for class 'Settings' is not found`（LocalStore.saveSettings）。
- 原因：`:app` 的 LocalStore.kt 用了 `@Serializable` 但模块没启用 `kotlin("plugin.serialization")`，读取被 `runCatching` 吞掉，保存时才崩。
- 修复：app/build.gradle.kts plugins 增加 `alias(libs.plugins.kotlin.serialization)`。修复后 1991/seven 显示 35 岁 + 五七原文，情志日记预设数据正常。

## 环境限制说明

- 本机无硬件虚拟化（WHPX 不可用），模拟器为 swiftshader 软渲染；小组件选择器在 320×480 下按钮互相遮挡无法点选，因此本批截图临时将 AVD 调到 320×720（同设备同系统镜像），拍完已还原。所有截图浅色模式（`ui_night_mode=2`）。
