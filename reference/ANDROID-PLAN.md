# 内经养生 Android 开发计划（v1，2026-11-18）

## GitHub 同类调研（2026-11-18）
- 空白：没有《上古天真》生命节律、《灵枢·通天》五态人类应用；内经类安卓应用多为学习/诊断工具。
- 直接用：6tail/tyme4j（MIT，lunar-javascript 作者的新库，Java）或 6tail/lunar-java（MIT）——安卓端节气推算，与网页 lunar-javascript 同源。
- 可借鉴（MIT）：xioceng/ziwu-clock（Java，十二时辰经络动态壁纸/表盘）；ShuyangLiu716/huangdi-neijing-body-calendar（每日身体黄历，守运动科学边界）；kangarooking/huangdi-neijing-skill（内经方法论拆解）。
- 只看思路（无许可证，不抄代码）：acuherb/ziwu、davids8644/LunarClock、gdhucoder/huangdi-neijing-reader、Kurumiiau/Xiangshouzhengdao。
- 反面参照：jangviktor-web/nihaixia-app（Flutter，301★，AI 辅助诊断）——走诊断路线，我们不做。

## 技术选型
- Kotlin + Jetpack Compose，minSdk 26，离线优先，无后端。
- 推算：P0 用 lunar-java（cn.6tail:lunar，lunar-javascript 的同源 Java 移植，保证与网页逐秒一致）；tyme4j 备选；把 web/wuyun-calc.js 移植为 Kotlin，与 JS 版在同一批 434 个时间点上逐点比对一致。
- 内容：与网页共用 JSON（cards.json 等），打进 assets；文案只改 JSON，代码不写死文案。
- 小组件：Jetpack Glance。本地记录：Room。分享图：Compose 绘制导出 PNG。

## 阶段
- P0 地基（约 1 周）：建仓 allinnb/wuyun-android；推算模块移植 + 单元测试比对 JS；JSON 资产加载。
- P1 运气日历（约 2 周）：今日卡、六步时间轴、点选、分享图、卡底声明。与线上网页验收表一致。
- P2 时令（约 2 周）：四季作息（四气调神，按节气）+ 十二时辰钟（灵枢·经脉）+ 桌面小组件。
- P3 引流（约 2 周）：生命节律卡（上古天真）+ 五态人测试（灵枢·通天），中英双语，分享图。
- P4 记录（约 3 周）：五味餐盘、情志日记（本地存储）；五音歌单（只用免版税音频）。内经解梦视情况。
- P5 上架：国内 APP 备案、软著、隐私政策，华为/小米/OPPO/vivo/应用宝；海外 Google Play（健康类声明）。归类“生活/文化”，不归医疗。

## 分工
- Hark：每阶段的内容 JSON、需求说明、验收表、上架文案与合规核对。
- DSH：代码、构建、APK。

## 合规红线
不做面诊、舌诊、脉诊、按症状推穴位/方剂；不写功效与剂量；卡底“传统文化参考，不舒服请就医”。
- P0 交给 Mistral 写代码（用户 2026-11-18 定）；P0 工具包 work/wuyun-android-p0.zip（含 reference/golden.json：1554 个时间点 + 2026–2087 stepsOf 的标准答案）。
