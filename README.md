# 五子棋 Android 小游戏

这是一个原生 Android 五子棋小游戏，使用 Java 自定义 View 绘制棋盘和棋子，无需额外第三方依赖。

## 功能

- 15 × 15 标准五子棋棋盘
- 黑棋先手，双人轮流落子
- 自动判断横向、纵向、两条斜线五连胜利
- 棋盘下满后自动判定平局
- 高亮显示最后一步落子
- 支持一键重新开始

## 运行方式

1. 用 Android Studio 打开仓库根目录。
2. 等待 Gradle 同步完成。
3. 运行 `app` 模块到模拟器或真机。

也可以在本地安装 Android Gradle 环境后执行：

```bash
gradle assembleDebug
```

应用包名：`com.example.gomoku`
