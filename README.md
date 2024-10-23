![ColdDev](https://imgur.com/9J5FHtB.png)
# ColdDev

Internal library used for **Cold Development** plugins.
> [!WARNING]
> If you ever find a `.jar` file of this lib `colddev.jar`, don't load it into your `~/plugins` folder because won't work.<br>
> Is just a library used for plugins.
<br>

> [!CAUTION]
> If you find a `ColdDev` folder into your `~/plugins`, **do not delete it**.<br>
* _(if you delete this folder that contains **SQLite databases**, your data goes bye bye, **so be careful**)_
<br>

> [!NOTE]
> * This folder is present to store **SQLite databases** that are used by default instead of **MySQL**.<br>
> * This folder has a configuration file where you can create `aliases` for your commands.<br>
> * This folder has a configuration file that checks for `plugin updates` that uses `ColdDev` library.

---
## </> For developers
<p>
    <a href="https://github.com/Cold-Development/ColdDev/releases">
        <img alt="spigot" src="https://img.shields.io/github/v/release/Cold-Development/ColdDev?display_name=release&style=for-the-badge&color=30a648"/>
    </a>
</p>

ColdDev is a standalone plugin library, so you will need to install it on any plugins that you build with this internal library.<br>

### Maven:
- Repository<br>

```pom.xml
<repository>
   <id>com.github.coldplugin</id>
   <url>https://maven.pkg.github.com/Cold-Development/ColdDev</url>
</repository>
```
- Dependency
  - Replace `{version}` with the latest version available; example `1.0`.<br>
  
```pom.xml
<dependency>
  <groupId>dev.padrewin</groupId>
  <artifactId>colddev</artifactId>
  <version>{version}</version>
  <scope>compile</scope> <!-- Make sure the scope is COMPILE. -->
</dependency>
```

![](https://raw.githubusercontent.com/mayhemantt/mayhemantt/Update/svg/Bottom.svg)
