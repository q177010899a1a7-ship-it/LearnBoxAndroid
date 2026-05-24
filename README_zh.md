# LearnBox - AI 智能学习笔记

<p align="center">
  <img src="screenshots/01_setup_screen.png" width="250" />
  <img src="screenshots/10_analysis_result.png" width="250" />
  <img src="screenshots/06_mindmap_tab.png" width="250" />
</p>

<p align="center">
  <b>AI 驱动的 Android 学习应用 | 视频分析 | 智能笔记 | 思维导图</b>
</p>

---

## 功能特性

- **AI 视频分析** - 自动提取视频关键帧，生成中文结构化分析
- **5大分析板块** - 视频摘要 / 关键要点 / 详细笔记 / 思维导图 / 学习建议
- **勾选标记** - 勾选重要内容、高亮关键文字、导出到笔记
- **智能笔记** - 创建和管理学习笔记，支持多种模板
- **思维导图** - 画布式思维导图，支持缩放、平移、贝塞尔曲线
- **多供应商 AI** - 多个免费 AI 供应商自动切换，智能冷却机制
- **提醒功能** - 设置学习提醒，支持重复提醒
- **全文搜索** - 跨内容全文搜索

## 应用截图

| 引导设置 | 视频列表 | AI 分析结果 |
|---------|---------|------------|
| ![引导](screenshots/02_siliconflow_card.png) | ![视频](screenshots/04_main_video_list.png) | ![分析](screenshots/10_analysis_result.png) |

| 笔记 | 思维导图 | 设置 |
|------|---------|------|
| ![笔记](screenshots/05_notes_tab.png) | ![导图](screenshots/06_mindmap_tab.png) | ![设置](screenshots/07_settings_tab.png) |

## 快速开始

### 第一步：获取 API Key

本应用支持多个免费 AI 供应商，**推荐使用硅基流动 (SiliconFlow)**：

| 供应商 | 免费模型 | 官网 |
|--------|---------|------|
| **硅基流动** (推荐) | 6个视觉模型 + 10个文本模型 | [siliconflow.cn](https://cloud.siliconflow.cn/i/cghBZBng) |
| Groq | 6个文本模型 (超快推理) | [console.groq.com](https://console.groq.com) |
| Google Gemini | 3个视觉模型 + 4个文本模型 | [aistudio.google.com](https://aistudio.google.com) |
| SambaNova | 3个文本模型 | [cloud.sambanova.ai](https://cloud.sambanova.ai) |

### 第二步：为什么硅基流动需要充值？

> **硅基流动的免费模型完全够日常使用**，但充值少量金额（2元 / ~$0.30）可以解锁以下好处：
>
> - **更高的请求限制** - 免费版有每日请求次数限制，充值后限制大幅提升
> - **解锁更多模型** - 部分大模型（如 Qwen3-VL-32B）需要余额才能使用
> - **优先队列** - 付费用户在高峰期享有更快的响应速度
> - **支持平台** - 硅基流动提供慷慨的免费额度，小额充值帮助他们持续提供免费服务
>
> **总结**：2元足够测试所有功能。免费模型（Qwen3-VL-8B、Qwen3.5-4B、DeepSeek-V4-Flash）日常使用完全够用。

### 第三步：安装使用

1. 从 [Releases](../../releases) 下载最新 APK
2. 安装到 Android 设备
3. 打开应用，按引导操作
4. 选择硅基流动，粘贴你的 API Key
5. 开始使用！

## 从源码构建

### 环境要求
- Android Studio Hedgehog (2023.1) 或更高版本
- Android SDK 34
- JDK 17

### 构建命令

```bash
git clone https://github.com/q177010899a1a7-ship-it/LearnBoxAndroid.git
cd LearnBoxAndroid
./gradlew assembleDebug
```

安装到设备：
```bash
adb install -r -t app/build/outputs/apk/debug/app-debug.apk
```

## 项目架构

```
app/src/main/java/com/learnbox/
  data/           # Room 数据库、实体、DAO、仓库
  service/        # AI 供应商、模型池、视频分析器
  ui/             # Compose 界面和 ViewModel
    mindmap/      # 思维导图画布和编辑器
    navigation/   # 主界面和底部导航
    note/         # 笔记列表和编辑器
    reminder/     # 提醒管理
    search/       # 搜索功能
    settings/     # 设置和供应商管理
    setup/        # 首次设置向导
    theme/        # 颜色和字体
    video/        # 视频列表和分析
```

### 技术栈

- **UI**: Jetpack Compose + Material3
- **数据库**: Room (SQLite)
- **AI 服务**: OpenAI 兼容 API，多供应商自动切换
- **视频处理**: MediaMetadataRetriever 提取关键帧
- **网络**: OkHttp，120秒超时适配大文件传输

### 多供应商自动切换

应用维护一个跨多个供应商的 AI 模型池：
- **智能轮询** - 在可用模型间均匀分配请求
- **自动切换** - 某个模型失败时自动尝试下一个
- **冷却机制** - 失败后 30秒 -> 2分钟 -> 10分钟 递增冷却
- **持久化配置** - 供应商设置通过 SharedPreferences 保存

## API Key 安全

- 你的 API Key **仅存储在本地设备** (SharedPreferences)
- Key **不会上传**到任何服务器
- 应用直接与 AI 供应商 API 通信
- 无分析、无追踪、无数据收集

## 项目结构

```
LearnBoxAndroid/
  app/
    build.gradle.kts          # 应用构建配置
    src/main/
      AndroidManifest.xml     # 应用清单
      java/com/learnbox/      # Kotlin 源码
      res/                    # 资源文件
  build.gradle.kts            # 项目构建配置
  gradle/                     # Gradle Wrapper
  screenshots/                # 应用截图
  README.md                   # 英文说明
  README_zh.md                # 中文说明 (本文件)
  .gitignore                  # Git 忽略规则
```

## 贡献

欢迎提交 Pull Request！

## 许可证

MIT License

## 致谢

- [硅基流动](https://cloud.siliconflow.cn/i/cghBZBng) 提供免费 AI 模型
- [Jetpack Compose](https://developer.android.com/jetpack/compose) 现代 UI 工具包
- [Room](https://developer.android.com/training/data-storage/room) 本地数据库
