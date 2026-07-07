# BPM Piano（横屏 BPM 钢琴节拍器）

## 功能
- 横屏界面，强制锁定横向显示
- 顶部：BPM 数字输入框 + "BPM" 文字标签（默认值 120，可修改，范围 20-300）
- 底部：**5 个按钮**，分别对应 C4 / D4 / E4 / G4 / A4（C 大调五声音阶）
- 点击某个按钮开始按当前 BPM 连续播放八分音符的钢琴音（合成音色，非采样音频），再次点击停止
- 多个按钮可同时按下，独立循环播放
- 钢琴音由代码实时合成（谐波叠加 + 指数衰减包络），无需任何音频素材文件

## ⚠️ 关于 APK 打包
我这边运行代码的环境网络受限（无法访问 Google Maven 仓库和 Android SDK 下载源），
**没办法在这个环境里直接帮你编译出 APK 文件**。给你两个真正能拿到 APK 的办法，选一个就行：

### 方案 A：GitHub Actions 自动打包（推荐，不用装任何东西）
1. 把这个压缩包解压后，整个 `BpmPiano` 文件夹内容推送到你自己的一个新 GitHub 仓库（网页上传或 `git push` 都行）
2. 仓库里已经包含 `.github/workflows/build-apk.yml`，push 之后 GitHub 会自动开始编译
3. 打开仓库的 **Actions** 标签页，等 2-3 分钟构建完成
4. 进入对应的构建记录，下拉找到 **Artifacts** 区域，下载 `bpm-piano-debug-apk`，解压后就是 `app-debug.apk`
5. 把这个 apk 传到手机上安装即可（需要在手机设置里允许"安装未知来源应用"）

### 方案 B：本地用 Android Studio 打包（几分钟搞定）
1. 打开 Android Studio → Open → 选择解压后的 `BpmPiano` 文件夹（里面已经是完整工程，含根目录 `build.gradle` / `settings.gradle`）
2. 等待 Gradle 同步完成
3. 菜单栏 **Build → Build Bundle(s) / APK(s) → Build APK(s)**
4. 构建完成后点弹窗里的 "locate" 就能找到生成的 `app-debug.apk`

两种方式生成的都是 **debug 版 APK**，可以直接安装测试。如果你需要发布到应用商店的**正式签名版**，需要额外配置签名密钥（keystore），告诉我我可以帮你补上签名配置。

## 音高自定义
在 `MainActivity.kt` 里修改这两行即可改变五个按钮对应的音符：

```kotlin
private val noteFrequencies = floatArrayOf(261.63f, 293.66f, 329.63f, 392.00f, 440.00f) // C4 D4 E4 G4 A4
private val noteNames = arrayOf("C4", "D4", "E4", "G4", "A4")
```

## 节奏计算说明
八分音符时值（毫秒）= (60000 / BPM) / 2，代码里对应：
```kotlin
val intervalMs = (30000.0 / bpm).toLong()
```
这个值会在每次触发时重新读取输入框的 BPM，所以你可以在播放过程中实时调整速度。
