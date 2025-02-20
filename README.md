# Modern UI
[![MavenCore](https://img.shields.io/badge/dynamic/xml?color=orange&label=Core%20Version&query=%2Fmetadata%2Fversioning%2Flatest&url=https%3A%2F%2Fmaven.izzel.io%2Freleases%2Ficyllis%2Fmodernui%2FModernUI-Core%2Fmaven-metadata.xml)]()
[![Discord](https://img.shields.io/discord/696234198767501363?color=green&label=Discord&style=flat)](https://discord.gg/kmyGKt2)
### Description
Modern UI (by Icyllis Milica) is a desktop application framework designed for standalone 2D and 3D rendering software development.
It makes use of modern 3D graphical APIs and technologies to provide high real-time rendering performance.
This framework is similar to JavaFX or Android, with a complete set of event loops, rendering systems, and UI components,
which are also suitable for game development.

There is also an official version that extends to Minecraft and Forge, it combines Modern UI with Minecraft and
provides a number of additional features and modding APIs. See [ModernUI-MC](https://github.com/BloCamLimb/ModernUI-MC) repository.

What are the advantages?  
Powerful UI functionality, good internationalization support, complete text layout engine based on HarfBuzz.
This framework has a powerful graphics engine, which is good to OpenGL 3.3 and OpenGL 4.5 core profiles and
is specifically optimized for desktop GPUs, some engine designs are better than Google Skia.

This project is still at a *relatively* early stage.  
Releases for Minecraft Mod are available on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/modern-ui).  
If you have any questions, feel free to join our [Discord](https://discord.gg/kmyGKt2) server.
### License
* Modern UI
  - Copyright (C) 2019-2023 BloCamLimb. All rights reserved.
  - [![License](https://img.shields.io/badge/License-LGPL--3.0--or--later-blue.svg?style=flat-square)](https://www.gnu.org/licenses/lgpl-3.0.en.html)
  - Copyright (C) 2006 The Android Open Source Project
  - [![License](https://img.shields.io/badge/License-Apache%202.0-orange.svg?style=flat-square)](https://opensource.org/licenses/Apache-2.0)
* Modern UI Assets
  - Copyright (C) 2019-2023 BloCamLimb et al.
  - [![License](https://img.shields.io/badge/License-CC%20BY--NC--SA%204.0-yellow.svg?style=flat-square)](https://creativecommons.org/licenses/by-nc-sa/4.0/)
* Libraries
  - [lwjgl](https://github.com/LWJGL/lwjgl3) licensed under the BSD-3-Clause
  - [fastutil](https://github.com/vigna/fastutil) by Vigna, licensed under the Apache-2.0
  - [caffeine](https://github.com/ben-manes/caffeine) by Ben Manes, licensed under the Apache-2.0
  - [flexmark-java](https://github.com/vsch/flexmark-java) by Atlassian Pty Ltd, Vladimir Schneider
  - [log4j](https://github.com/apache/logging-log4j2) licensed under the Apache-2.0
  - [icu4j](https://github.com/unicode-org/icu) by Unicode, Inc.
### Documentation
(WIP)

#### Environment requirements
- Windows 10 or above, Linux or macOS
- JDK 17.0.1 or above
- OpenGL 3.3 or above
- Vulkan 1.1 or above (WIP)

#### Gradle configuration
```
repositories {
    maven {
        name 'IzzelAliz Maven'
        url 'https://maven.izzel.io/releases/'
    }
}
dependencies {
    implementation "icyllis.modernui:ModernUI-Core:${modernui_core_version}"
    // apply appropriate LWJGL platform here (mandatory)
    // apply other Modern UI modules (optional)
}
```
