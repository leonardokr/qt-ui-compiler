# Qt UI Compiler

[![JetBrains Plugins](https://img.shields.io/jetbrains/plugin/v/29493.svg)](https://plugins.jetbrains.com/plugin/29493-qt-ui-compiler)
[![JetBrains Plugins](https://img.shields.io/jetbrains/plugin/d/29493.svg)](https://plugins.jetbrains.com/plugin/29493-qt-ui-compiler)
[![JetBrains Plugins](https://img.shields.io/jetbrains/plugin/r/rating/29493.svg)](https://plugins.jetbrains.com/plugin/29493-qt-ui-compiler)

Automated conversion of Qt Designer `.ui` files to Python (`PyQt6` or `PySide6`) for IntelliJ-based IDEs.

## Overview

The **Qt UI Compiler** is an extension for Python developers that automates the generation of Python code from Qt XML
user interface files. It ensures that the visual design remains synchronized with the implementation by monitoring
changes and invoking the appropriate compilation tools (`pyuic6` or `pyside6-uic`) automatically.

- **Automatic Compilation:** Detects modifications in `.ui` files and generates the corresponding `.py` files instantly
  upon saving.
- **Manual Execution:** Provides context menu actions and keyboard shortcuts (`Ctrl+Alt+U`) for on-demand compilation.
- **Environment Awareness:** Supports virtual environments (`venv`) and custom executable paths.
- **Flexible Configuration:** Offers glob pattern filtering for targeted file monitoring and customizable output paths.
- **I18n Support:** Fully internationalized interface and notifications.
- **Cross-Platform:** Compatible with Windows, macOS, and Linux.

## Installation

The plugin can be installed via the JetBrains Marketplace within any compatible IDE (IntelliJ IDEA, PyCharm, etc.):

1. Navigate to **Settings/Preferences | Plugins**.
2. Search for **Qt UI Compiler**.
3. Click **Install**.

## Configuration

Settings are located under **Settings/Preferences | Tools | Qt UI Compiler**.

### Environment Settings

- **Virtual Environment Path:** Specify the path to the project's virtual environment (default: `.venv`).
- **Custom UIC Path:** Optional path to a specific `uic` executable if auto-detection is not sufficient.
- **Relative Paths:** Toggle whether the paths above are interpreted relative to the project root.

### Compilation Settings

- **Enable auto-compilation:** Globally enable or disable background compilation on save.
- **UI Files Filter:** Use glob patterns (e.g., `**/ui/*.ui`) to define which files the plugin should process (default:
  `*.ui`).
- **Output Path/Pattern:** Define where the generated `.py` files should be stored. Use `$1` as a placeholder for the
  original filename.

## Usage

1. Configure the environment and compilation settings.
2. The plugin will monitor files matching the defined pattern.
3. Upon saving a `.ui` file, the corresponding `.py` file is generated or updated.
4. Alternatively, right-click a `.ui` file in the **Project View** or **Editor** and select **Compile UI File**.

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

## License

This project is licensed under the terms specified in the repository.
