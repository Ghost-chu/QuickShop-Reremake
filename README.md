# QuickShop-Reremake

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/f72f3d1577064834b0495073fd53b4bd)](https://app.codacy.com/gh/Ghost-chu/QuickShop-Reremake?utm_source=github.com&utm_medium=referral&utm_content=Ghost-chu/QuickShop-Reremake&utm_campaign=Badge_Grade_Settings)

QuickShop is a shop plugin, that allows players to sell items from a chest with no commands.  It allows players to purchase any number of items easily.  In fact, this plugin doesn't even have any commands that a player would ever need!  

QuickShop-Reremake is a fork for QuickShop NotLikeMe, QuickShop-Reremake has more features and bug fixes.  

QuickShop-Reremake makes by PotatoCraft Studio from https://github.com/KaiKikuchi/QuickShop upstream repo.  

## Support
Open a new issue here: https://github.com/Ghost-chu/QuickShop-Reremake/issues

## Features
- Easy to use
- Togglable Display Item on top of the chest
- NBT Data, Enchants, Tool Damage, Potion, and Mob Egg support
- Unlimited chest support
- Blacklist support & bypass permissions
- Shops that buy items and sell items at the same time are possible (Using double chests)
- Checks a player can open a chest before letting them create a shop!
- UUID support
- More shop protection! [Reremake]
- Item display name i18n! [Reremake]
- Enchantment display name i18n! [Reremake]
- A cool item preview [Reremake]
- Friendly for the region and world protect plugin [Reremake]
- ProtocolLib based Virtual DisplayItem support [Reremake]
- Powerful API [Reremake]
- Optimized performance [Reremake]


## Builds
Github projects have a "releases" link on their home page. If you still don't see it, [click here](https://github.com/Ghost-chu/QuickShop-Reremake/releases) for QuickShop-Reremake stable release.  
Or use our [CodeMC.io Jenkins](https://ci.codemc.io/job/Ghost-chu/job/QuickShop-Reremake/) for snapshot builds.

## Links
- [SpigotMC](https://www.spigotmc.org/resources/62575/)  
- [BukkitDev](https://dev.bukkit.org/projects/quickshop-reremake)  
- [Minecraft Chinese Forum (MCBBS)](http://www.mcbbs.net/thread-809496-1-1.html)
- [Relatev](http://www.relatev.com/forum.php?mod=viewthread&tid=2251)
- [Jenkins](https://ci.codemc.io/job/Ghost-chu/job/QuickShop-Reremake/)

## Contribute
If you're a developer, you can contribute to the QuickShop code! Just make a fork and install Lombok plugin, then make a pull request when you're done! Please try to follow [Google Java Style](https://google.github.io/styleguide/javaguide.html). Also do not increase the plugin version number. Thank you very much!

## Maven
```XML
<repository>
    <id>quickshop-repo</id>
    <url>https://repo.codemc.io/</url>
</repository>

<dependency>
    <groupId>org.maxgamer</groupId>
    <artifactId>QuickShop</artifactId>
    <version>{VERSION}</version>
    <scope>provided</scope>
</dependency>
```
