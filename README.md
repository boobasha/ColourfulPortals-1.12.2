# 多彩传送门 / Colourful Portals — 1.12.2

[![Language](https://img.shields.io/badge/语言-中文-brightgreen?style=for-the-badge)](README.md)
[![Language](https://img.shields.io/badge/Language-English-blue?style=for-the-badge)](README_EN.md)

> 🌐 Language: **中文** · Click the **English** badge above to switch.

将 Tmtravlr 的 **Colourful Portals**（多彩传送门）从 Minecraft **1.7.10** 移植到 **1.12.2 (Forge)**，移植版本 **1.0.1**。所有原版贴图、音效、语言文本均沿用（仅按 1.12.2 要求把个别贴图文件名改为小写）。

## 简介

用羊毛 / 染色陶瓦 / 染色玻璃搭一个框架，倒入「多彩之水」即可生成传送门。**相同颜色**的传送门会互相连接，可以跨维度传送。也可以合成单方块的「独立传送门」。

## 功能

- **多彩之水**流体 + 一整套水桶进阶（染料桶 → 混合 → 附魔防染色桶）。
- **框架传送门**：羊毛 / 染色陶瓦 / 染色玻璃，共 16 种颜色，同色互联（含跨维度）。
- **独立传送门**：可合成的单方块传送门（16 色，3 种框架材质）。
- **多彩末影珍珠**（普通 / 高反射）：右键或丢入传送门以创建随机目的地。
- 粒子、传送音效、配置文件、合成表、地牢宝箱掉落。
- 内置简体中文与英文语言文件。

## 安装

把 `ColourfulPortals-1.12.2-1.0.1.jar` 放进 1.12.2 Forge 的 `mods` 文件夹即可。

## 构建

需要 **JDK 8**。本仓库 `gradle.properties` 里的 `org.gradle.java.home` 默认指向 `C:/Program Files/Zulu/zulu-8`，请改成你机器上的 JDK 8 路径（或删掉该行并自行设置 `JAVA_HOME`）。

```bash
# Windows 命令行 / PowerShell（启动器需要 JAVA_HOME 指向 JDK 8）：
set JAVA_HOME=C:\Program Files\Zulu\zulu-8
gradlew.bat build

# 或 Git Bash：
JAVA_HOME="/c/Program Files/Zulu/zulu-8" ./gradlew build
```

产物位于：`build/libs/ColourfulPortals-1.12.2-1.0.1.jar`

其它常用命令：

```bash
gradlew build        # 编译并打包 jar
gradlew runClient    # 启动开发客户端
gradlew runServer    # 启动开发服务端
```

### 工具链说明（为什么是这些版本）

- **ForgeGradle 2.3**（`2.3-20210802.170449-48`）配 **Gradle 4.10.3**。
- **Forge `1.12.2-14.23.5.2847`** —— 不是最终的 2860。Forge 2854+ 在 Maven 上把 FG2 格式的 `userdev` 换成了 `userdev3`（ForgeGradle 3 的格式），ForgeGradle 2.3 无法使用；2847 是最后一个仍带 FG2 `userdev` 的 1.12.2 版本，因此能直接拉取。
- 已配置国内镜像（腾讯 Gradle 发行版 + 阿里云 Maven），首次构建更快。
- 第一次构建会下载 Forge 并反混淆 Minecraft，需要一些时间和网络。

## 移植说明

1.7.10 → 1.12.2 几乎重写了整个 API：`cpw.mods.fml.*` → `net.minecraftforge.fml.*`；坐标 `int x,y,z` → `BlockPos`；元数据 → `IBlockState`（一个 0–15 的 `color` 属性）；`IIcon` 渲染 → JSON 模型 / blockstate；流体用 `forge:fluid` 模型；跨维度传送改用 `transferPlayerToDimension` / `Entity.changeDimension` + 自定义 `ITeleporter`。独立传送门的自定义 GL 渲染（1.7.10 的 `ISimpleBlockRenderingHandler` 已不存在）重写为 `TileEntitySpecialRenderer` + 物品 `TileEntityItemStackRenderer`。

## 许可与致谢

- 原模组作者：**Tmtravlr**。
- 本仓库为社区移植版本，沿用原模组的 **GPL-3.0** 许可（见 [LICENSE](LICENSE)）。
