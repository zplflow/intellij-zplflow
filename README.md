# zplflow for JetBrains IDEs (IntelliJ IDEA, GoLand, WebStorm, PyCharm)

Official JetBrains IDE plugin for **zplflow** — pixel-perfect ZPL thermal label live preview via JCEF, `.zpl` file recognition, and dynamic DPI switching (152 / 203 / 300 / 600 DPI).

## Features

- **ZPL File Type** registration for `.zpl`, `.prn`, and `.label`.
- **JCEF Live Preview ToolWindow** powered by `POST /v1/pipelines/preview` (0 token charge).
- **SHA-256 LRU Cache** & rate-limit handling (`Retry-After`, `X-RateLimit-Remaining`).
