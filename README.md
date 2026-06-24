# Mindustry-ModLib (UNOFFICIAL)

Library to help developers creating Mindustry mods and optimize some game features  
If you think some utility or optimization should be added open an issue describing the feature.
Many features in this mod could be broken and not working due to lack of any useful for this project skills.

~~✔ **Fully compatible with vanilla servers/clients !**~~ Compatibility with servers and clients is untested

Current development tools:
-----

* Class mixins
* No packet limit
* Packet chaining (sending replies to received packets)
* `Call` like classes supporting custom parameters and return values
* Synchronized registrable types in TypeIO
* Improved logger

> **Note on Keybinds**: Custom keybind registration was overhaul-deprecated and removed due to native keybind support implemented in Mindustry v8

Mod Installation:
----
Desktop/Steam you can download the mod from the mod browser or from the releases page (ModLib).  
Android: Coming soon (?) ~~Download the mod launcher from the [releases pages](https://github.com/Redstonneur1256/Mindustry-ModLib/releases/), install the application and follow the steps from the application.~~

Using the library:
-----

- Add the dependency on Gradle:
  ```groovy
    repositories {
        mavenCentral()
        maven { url "https://jitpack.io/" }
    }
    
    dependencies {
        compileOnly("com.github.Redstonneur1256.Mindustry-ModLib:Mod:VERSION")
    }
  ```
- Update your `mod.json`/`plugin.json` to add the library as a dependency.
  ```json
  "dependencies": [
    "!mod-library"
  ]
  ```
- If using access-wideners you will require the [gradle-access-widener](https://github.com/Redstonneur1256/GradleAccessWidener),
  please refer to the plugin's documentation for configuration instructions.

Mixin/Access widener files must be present at the root of the mod's file structure, the files name excluding the extension
must be the exact same than the `name` property defined in your `mod.(h)json`/`plugin.(h)json`.

See the `Example` module for usage examples.

Contributing:
------

See [CONTRIBUTING](CONTRIBUTING.md)

Contributors:
-------

Special thanks to [Eliott SRL](https://github.com/Eliott-Srl) for making the icon.
