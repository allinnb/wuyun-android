# P2–P4 交接说明（Hark → Mistral，2026-11-19）

基于 P1 交付包（P1 已由 Hark 独立复验：`:core:test` 12/12 通过，JSON 与网页版逐字一致）。
本包新增 P2–P4 的内容 JSON、`:core` 逻辑与测试、`:app` 界面初稿。

## Hark 已完成并验证
- 内容（`app/src/main/assets/`，文案一个字不改）：
  - `seasons.json` 四季作息（《素问·四气调神大论》）
  - `shichen.json` 十二时辰（子午流注作息参考 +《灵枢·顺气一日分为四时》）
  - `lifecycle.json` 生命节律（《素问·上古天真论》，中英双语）
  - `wutai.json` 五态人测试（《灵枢·通天》，识典本；8 题，中英双语）
  - `wuwei.json` 五味餐盘、`emotions.json` 情志日记、`wuyin.json` 五音（tracks 暂空）
- `:core/life/`：`LifeModels.kt`、`LifeData.kt`、`LifeCalc.kt`
  - SeasonCalc（四立交节，lunar-java 节气表）、ShichenCalc、ReminderPlanner（六气交节前 3 天 09:00）、LifecycleCalc、WutaiQuiz、PlateScorer、DiaryStats
- `core/src/test/.../LifeTest.kt`：9 个测试（四季 2026–2087 逐日扫描、时辰边界、提醒、节律、测试题、餐盘、日记、内容规范扫描）
- Hark 沙箱结果：`:core:test` 共 21 个测试全部通过（Golden 7、Presenter 2、ShareImages 3、Life 9）。

## Hark 写了但没编译过（沙箱是 aarch64，无 Android SDK）
- `MainActivity.kt`：底部 4 个 Tab（今日 / 时令 / 测一测 / 记录）
- `ui/Common.kt`、`ui/SeasonScreen.kt`、`ui/TestScreen.kt`、`ui/RecordScreen.kt`
- `life/LocalStore.kt`：日记、餐盘、设置存 filesDir 的 JSON（不用 Room，不加依赖）
- `WuyunRepository.life`、`strings.xml` 新增字符串
→ 请先让它编译通过。只修编译错误和明显的布局问题，不改 JSON、不改 :core 的算法和测试期望值。

## 请 Mistral 完成
1. 编译修复：`./gradlew :core:test assembleDebug lint` 全过。
2. P2 交节提醒：用 `ReminderPlanner.next(now)`，AlarmManager（setAndAllowWhileIdle）+ BroadcastReceiver；Android 13+ 申请 POST_NOTIFICATIONS，用户在“时令”页打开开关后才申请；开机后（BOOT_COMPLETED）重排。通知标题取 `stepName`，正文用 strings.xml 新增一条：`%1$s 后交%2$s，新的一步气要来了`。设置存 `LocalStore.Settings.remind`。
3. P2 桌面小组件：用 AppWidgetProvider + RemoteViews（不引入 Glance 也可以），显示 今日卡 title + headline 第一段 + 当前时辰与 tip；每 30 分钟或时辰切换时更新；点按打开 App。
4. P3 分享图：生命节律与五态人结果各出一张 1080×1440 分享图，复用 `ShareImages`；文字用当前语言；二维码仍指向 https://allinnb.github.io/wuyun/
5. P3 英文：测一测页已有中英切换；只有该页双语，其余页面中文。
6. P4 五音：tracks 为空时整块只显示五音说明，不显示播放器。真要放音乐，只用 CC0 / 公有领域音频，在 tracks 里写 license 与 sourceUrl，放 assets/audio/。
7. 深色模式、字体放大到 1.3 倍时不截字。

## 验收（全部通过才算完成）
A. `:core:test` 21/21 通过（不得改测试期望值）。
B. D 规则继续：除 strings.xml 与 JSON 外，Kotlin/XML 无中文字面量（测试与注释除外）。
C. 截图（reference/screens-p2p4/）：时令页、生命节律（seven，出生 1991）、五态人结果、五味餐盘、情志日记（含关心提示），浅色各一张；小组件一张；通知一张。
D. 用 `--es date 2026-11-19T09:30` 启动时，时令页显示“冬三月”，时辰为“巳”。
E. 不申请除 POST_NOTIFICATIONS、RECEIVE_BOOT_COMPLETED、SCHEDULE_EXACT_ALARM（若用）以外的权限；仍不要 INTERNET。
F. 推到 allinnb/wuyun-android 的 main，TEST-REPORT.md 追加 P2–P4 一节。

## 规矩（同 P1）
- 文案只来自 JSON / strings.xml，不自己加文案；不写功效、剂量、诊断。
- 卡底“传统文化参考，不舒服请就医”每页都有（已放在各页 Footer）。
- 测试对不上时列出 input / 期望 / 实际，不改期望值；同一问题两次修不好就停下报告。

## 内容待办（Hark）
- 四季、上古天真、宣明五气（“是谓五入”）、阴阳应象、举痛论原文与识典底本逐字复核。
- P1 小问题：`ShareImagesTest` 样图里 headline 是占位词“headline”，换成真实 headline 再出样图。
