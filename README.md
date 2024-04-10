# ItemVoid

Item VoidHopper, collecting everything!

## Dependencies

* [NBTAPI](https://modrinth.com/plugin/nbtapi)

## Description

In RIA, whole server built up with over 60,000 different special NBT items. Even we build up lots of repository, but still have some items get lost in chest ocean or get lost forever.

ItemVoid will collect the items which have custom name or custom lores, and store it into database. And provide commands to allow users search by name or lores.

You can take out items from query result GUI when you need.

<img width="813" alt="304310349-129a4b19-d953-4169-a722-29dadfae9834" src="https://github.com/Ghost-chu/ItemVoid/assets/30802565/f9052d4c-f64b-4d03-936c-7d93caa60e4e">

## Permission

* itemvoid.use - To access all features provide by ItemVoid

## Commands

/itemvoid queryName <name> - Search by name  
/itemvoid queryLore <lore> - Search by lores 
/itemvoid status - Check save queue status  
/itemvoid forcesaveall - Flush all items that pending to save from memory to database for search 
