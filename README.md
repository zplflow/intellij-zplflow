# zplflow for JetBrains IDEs (IntelliJ IDEA, GoLand, WebStorm, PyCharm)

Official JetBrains IDE plugin for **zplflow** — pixel-perfect ZPL thermal label live preview, built-in Language Server (LSP 3.17) with 188 official Zebra commands, real-time parameter breakdown, and multi-DPI resolution switching.

---

## ✨ Features

### ⚡ 1. Embedded Language Server (LSP 3.17)
- **188 Zebra Commands Supported**: Full syntax and autocompletion for control commands (`^XA`, `^XZ`), format commands (`^FO`, `^FD`, `^FS`), barcodes (`^BC`, `^BQ`, `^B3`), graphics (`^GF`, `^GB`), and comments (`^FX`).
- **In-Depth Parameter Documentation**: Hover or autocomplete any command to see argument names, types, valid values, and default settings directly in the editor.
- **Interactive Signature Help**: Shows the active argument as you type commas.
- **Zero Configuration**: Resolves precompiled binaries automatically on Linux, macOS, and Windows.

### 🖨️ 2. JCEF Live Preview ToolWindow
- **Pixel-Perfect Rendering**: Side-by-side interactive preview in the right sidebar ToolWindow (`zplflow Preview`).
- **Dynamic Resolution Switching**: 152 DPI (6 dpmm), 203 DPI (8 dpmm), 300 DPI (12 dpmm), 600 DPI (24 dpmm).
- **Fast Local Caching**: In-memory SHA-256 LRU cache for lag-free undo/redo.
- **0 Token Charge**: Live preview requests use `POST /v1/pipelines/preview` with zero billing cost.

### 📄 3. File Type Registration
- Recognizes `.zpl`, `.prn`, and `.label` files automatically with custom icons.

---

## 🚀 How to Run & Test in IntelliJ IDEA

### Method 1: Run via Gradle (`runIde`)
1. Open IntelliJ IDEA.
2. Select **File → Open** and select the directory:
   ```
   /home/valerio/Progetti/intellij-zplflow
   ```
3. Wait for IntelliJ to sync the Gradle project.
4. In the **Gradle** tool window (right sidebar), navigate to:
   ```
   Tasks → intellij platform → runIde
   ```
   *(Or run `./gradlew runIde` from the built-in terminal).*
5. A sandbox instance of IntelliJ IDEA will launch with the **zplflow** plugin installed and active.
6. Open any `.zpl` file to test autocompletion, hover docs, and the **zplflow Preview** tool window on the right.

### Method 2: Build the Distributable Plugin Zip
To create the plugin archive for manual installation or uploading to the JetBrains Marketplace:
1. In the Gradle tool window, run:
   ```
   Tasks → intellij platform → buildPlugin
   ```
   *(Or run `./gradlew buildPlugin` from the terminal).*
2. The distributable `.zip` file will be generated in:
   ```
   build/distributions/intellij-zplflow-0.2.0.zip
   ```
3. To install locally in any JetBrains IDE:
   - Go to **Settings / Preferences → Plugins**.
   - Click the gear icon ⚙️ → **Install Plugin from Disk...**.
   - Select the generated `.zip` file.

---

## ⚙️ Configuration & Settings

Available under **Settings / Preferences → Tools → zplflow**:
- **Enable Language Server (LSP)**: Toggle background LSP engine on/off.
- **LSP Binary Path**: Optional custom path to `zplflow-lsp` executable (auto-detected if empty).
- **API URL**: Base URL (`https://api.zplflow.com` or custom self-hosted gateway).
- **API Key**: Optional API key for zplflow cloud services.
- **Default Resolution**: 152, 203, 300, or 600 DPI.

---

## 📄 License

Licensed under the [MIT License](LICENSE.md).
