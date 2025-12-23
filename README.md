# Qt UI Compiler Plugin

IntelliJ plugin to automatically compile PyQt6/PySide6 .ui files.

## Features

- Automatic compilation of .ui files when changes are detected
- Flexible virtual environment and UIC executable configuration
- Support for custom file patterns
- Output path configuration with regex support
- Manual action to compile specific .ui files
- Auto-detection of PyQt6 vs PySide6

## Configuration

Go to **File > Settings > Tools > Qt UI Compiler** to configure:

- **Virtual Environment Path**: Path to virtual environment (default: .venv)
- **UIC Path**: Path to pyside6-uic or pyuic6 executable (auto-detected if empty)
- **UI File Pattern**: Pattern for .ui files (default: **/*.ui)
- **Output Path**: Output path (default: same location as .ui file)
- **Enable auto-compilation**: Enable automatic compilation
- **Use relative paths**: Use paths relative to project

## Usage

1. Configure the plugin in settings
2. Edit .ui files - they will be compiled automatically
3. Or right-click on a .ui file and select "Compile UI File"

## Build

### Prerequisites
- Java 17 or higher
- Gradle 8.0+ (or use the wrapper)

### Building the Plugin

**Option 1: Using Gradle (if installed)**
```bash
gradle buildPlugin
```

**Option 2: Download Gradle**
1. Download Gradle from https://gradle.org/releases/
2. Extract and add to PATH
3. Run: `gradle buildPlugin`

**Option 3: Use IntelliJ IDEA**
1. Open project in IntelliJ IDEA
2. Run Gradle task: `buildPlugin`

The built plugin will be in `build/distributions/qt-ui-compiler-1.0-SNAPSHOT.zip`

### Installing for Testing
1. Go to **File > Settings > Plugins**
2. Click gear icon > **Install Plugin from Disk**
3. Select the .zip file from `build/distributions/`
4. Restart IntelliJ IDEA

## Publishing to JetBrains Marketplace

### 1. Prepare for Publishing
Update `build.gradle.kts`:
```kotlin
group = "com.ziondev"
version = "1.0.0"

patchPluginXml {
    changeNotes.set("""
        <ul>
            <li>Initial release</li>
            <li>Auto-compile .ui files on change</li>
            <li>Support for PyQt6 and PySide6</li>
            <li>Configurable paths and patterns</li>
        </ul>
    """)
}
```

### 2. Create JetBrains Account
1. Go to https://plugins.jetbrains.com/
2. Sign in with JetBrains account
3. Go to "Upload plugin"

### 3. Upload Plugin
1. Build plugin: `gradle buildPlugin`
2. Upload the .zip file from `build/distributions/`
3. Fill in plugin details:
   - **Name**: Qt UI Compiler
   - **Description**: Automatically compile PyQt6/PySide6 .ui files
   - **Category**: Build Tools
   - **Tags**: PyQt6, PySide6, Qt, UI, Python

### 4. Plugin Review Process
- JetBrains reviews all plugins
- Usually takes 1-3 business days
- You'll receive email notifications about status

### 5. Automated Publishing (Optional)
Add to `build.gradle.kts`:
```kotlin
publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
}
```

Get token from: https://plugins.jetbrains.com/author/me/tokens