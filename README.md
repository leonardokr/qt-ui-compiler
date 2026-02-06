# Qt UI Compiler

[![JetBrains Plugins](https://img.shields.io/jetbrains/plugin/v/29493.svg)](https://plugins.jetbrains.com/plugin/29493-qt-ui-compiler)
[![JetBrains Plugins](https://img.shields.io/jetbrains/plugin/d/29493.svg)](https://plugins.jetbrains.com/plugin/29493-qt-ui-compiler)
[![JetBrains Plugins](https://img.shields.io/jetbrains/plugin/r/rating/29493.svg)](https://plugins.jetbrains.com/plugin/29493-qt-ui-compiler)

Automated conversion of Qt Designer `.ui` files for IntelliJ-based IDEs.

## Overview

The **Qt UI Compiler** is an extension that automates the generation of code from Qt Designer XML user interface files. It ensures that the visual design remains synchronized with the implementation by monitoring changes and invoking the appropriate compilation tools automatically.

### Supported Frameworks

| Framework | Executable | Output |
|-----------|------------|--------|
| **PyQt6** | `pyuic6` | Python |
| **PySide6** | `pyside6-uic` | Python |
| **PyQt5** | `pyuic5` | Python |
| **PySide2** | `pyside2-uic` | Python |
| **Native Qt** | `uic` | C++ headers |

### Supported IDEs

Works with all JetBrains IDEs:
- **PyCharm** (Community & Professional)
- **IntelliJ IDEA** (Community & Ultimate)
- **CLion**
- **WebStorm**
- **Rider**
- And other IntelliJ-based IDEs

## Features

- **Automatic Compilation:** Detects modifications in `.ui` files and generates the corresponding output files instantly upon saving.
- **Manual Execution:** Provides context menu actions and keyboard shortcuts (`Ctrl+Alt+U`) for on-demand compilation.
- **Environment Awareness:** Supports virtual environments (`venv`) and custom executable paths.
- **Flexible Configuration:** Offers glob pattern filtering for targeted file monitoring and customizable output paths.
- **Auto-Detection:** Automatically finds the appropriate UIC executable in your virtual environment or system PATH.
- **I18n Support:** Fully internationalized interface and notifications.
- **Cross-Platform:** Compatible with Windows, macOS, and Linux.

## Installation

The plugin can be installed via the JetBrains Marketplace within any compatible IDE:

1. Navigate to **Settings/Preferences | Plugins**.
2. Search for **Qt UI Compiler**.
3. Click **Install**.

## Configuration

Settings are located under **Settings/Preferences | Tools | Qt UI Compiler**.

### Environment Settings

- **Virtual Environment Path:** Specify the path to the project's virtual environment (default: `.venv`).
- **Custom UIC Path:** Optional path to a specific `uic` executable if auto-detection is not sufficient.
- **Path Mode:** Toggle whether the paths above are interpreted relative to the project root.

### Compilation Settings

- **Enable auto-compilation:** Globally enable or disable background compilation on save.
- **UI Files Filter:** Use glob patterns (e.g., `**/ui/*.ui`) to define which files the plugin should process (default: `*.ui`).
- **Output Path/Pattern:** Define where the generated files should be stored. Use `$1` as a placeholder for the original filename.

## Usage

1. Configure the environment and compilation settings (or use auto-detection).
2. The plugin will monitor files matching the defined pattern.
3. Upon saving a `.ui` file, the corresponding output file is generated or updated.
4. Alternatively, right-click a `.ui` file in the **Project View** or **Editor** and select **Compile UI File**.

### For CLion / C++ Users

If you're using Qt with C++, the plugin will automatically detect the native `uic` executable in your PATH. Make sure Qt's bin directory is in your system PATH, or configure a custom UIC path in the settings.

## Building from Source

### Prerequisites

- Java 17
- Gradle (provided via wrapper)

### Build Steps

To build the plugin distribution:
```bash
./gradlew buildPlugin
```

The resulting ZIP file will be located in `build/distributions/`.

### Running Tests

```bash
./gradlew test
```

## License

This project is licensed under the terms specified in the [LICENSE](LICENSE) file.
