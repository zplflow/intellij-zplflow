# ZplFlow for JetBrains IDEs (IntelliJ IDEA, GoLand, WebStorm, PyCharm)

Official JetBrains IDE plugin for **ZplFlow** — pixel-perfect ZPL thermal label live preview, built-in Language Server (LSP 3.17) with 188 official Zebra commands, real-time parameter breakdown, native syntax highlighting, code formatting, and multi-DPI resolution switching.

---

## ✨ Features

### ⚡ 1. Embedded Language Server (LSP 3.17 - Runs Offline)
- **188 Zebra Commands Supported**: Full syntax and autocompletion for control commands (`^XA`, `^XZ`), format commands (`^FO`, `^FD`, `^FS`), barcodes (`^BC`, `^BQ`, `^B3`), graphics (`^GF`, `^GB`), and comments (`^FX`).
- **In-Depth Parameter Documentation**: Hover or autocomplete any command to see argument names, types, valid values, and default settings directly in the editor.
- **Interactive Signature Help**: Shows the active argument as you type commas.
- **Zero External Dependencies**: Bundled cross-platform binaries for Linux, macOS, and Windows.

### 🖨️ 2. JCEF Live Preview ToolWindow (Cloud-Powered)
- **Pixel-Perfect Rendering**: Side-by-side interactive preview in the right sidebar ToolWindow (`ZplFlow Preview`). Powered by ZplFlow Cloud API.
- **Free Live Preview**: Live preview requests use `POST /v1/pipelines/preview` with **0 token cost**. Requires a free API key from [zplflow.io](https://zplflow.io).
- **Interactive Dimensions & Presets**: Choose standard presets (`4" × 6"`, `4" × 4"`, `4" × 3"`, `3" × 2"`, `2" × 1"`) or adjust width/height in millimeters directly from the preview toolbar.
- **Multi-DPI Resolution Switching**: Switch dynamically between 152 DPI (6 dpmm), 203 DPI (8 dpmm), 300 DPI (12 dpmm), and 600 DPI (24 dpmm).
- **Multi-Page Navigation**: Flip through multiple labels with `◀` and `▶` page controls.
- **Fast Local Caching**: In-memory SHA-256 LRU cache for instant undo/redo.

### 🎨 3. Native Syntax Highlighting & Formatting
- **Syntax Highlighter**: Clear distinction between commands (`^FO`, `^FD`), numeric parameters (`50,50`), string data (`Hello World`), commas, and control keywords (`^XA`, `^XZ`, `^FS`).
- **Color Scheme Customization**: Configurable in **Settings → Editor → Color Scheme → ZPL**.
- **Reformat Code**: Reformat ZPL labels cleanly with `Ctrl + Alt + L` (Code → Reformat Code) or right-click → **Format ZPL Label**.
- **Auto-Indent on Enter**: Automatically aligns commands inside `^XA...^XZ` blocks with 2 spaces.

### 📄 4. File Type Registration
- Recognizes `.zpl`, `.prn`, and `.label` files automatically with custom icons.

---

## 🚀 How to Run & Test in IntelliJ IDEA / GoLand

### Method 1: Install the Prebuilt Plugin Zip
1. Open GoLand or IntelliJ IDEA.
2. Go to **Settings / Preferences (`Ctrl+Alt+S`) → Plugins**.
3. Click the gear icon ⚙️ → **Install Plugin from Disk...**.
4. Select:
   ```
   build/distributions/intellij-zplflow-0.2.1.zip
   ```
5. Click **Apply** / **OK** and restart IDE if prompted.
6. Configure your free API key under **Settings → Tools → ZplFlow** (click **Get your free API key**).
7. Open any `.zpl` file and view the **ZplFlow Preview** panel on the right sidebar.

### Method 2: Build from Source
```bash
./gradlew buildPlugin
```
The distributable `.zip` file will be generated in `build/distributions/`.

---

## ⚙️ Configuration & Settings

Available under **Settings / Preferences → Tools → ZplFlow**:
- **API Key**: Your ZplFlow API key (`zpl_live_...`). Click **Get your free API key** to create one for free.
- **API URL**: Base URL (`https://api.zplflow.io` or custom gateway).
- **Default DPI**: 152, 203, 300, or 600 DPI.
- **Label Width / Height (mm)**: Custom dimensions (defaults to 101.6 x 152.4 mm, 4x6").
- **Preview Mode**: `onSave` or `onType`.
- **Debounce Delay (ms)**: Real-time typing debounce (default 450ms).
- **Enable Language Server (LSP)**: Toggle LSP engine on/off.
- **Custom LSP Binary Path**: Optional custom path to `zplflow-lsp` executable (auto-detected if empty).

---

## 📄 License

Licensed under the [MIT License](LICENSE.md).
