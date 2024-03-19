# ItemVoid

物品NBT漏斗，收纳一切！

## 依赖

* [NBTAPI](https://modrinth.com/plugin/nbtapi)
* 一台 MySQL 服务器 (MySQL 5.7 或者更高版本)

## 描述

在 RIA，整个服务器由大量的自定义物品驱动。这些物品的数量实在是太多，即使建设了多个仓库，也相当难以查找对应物品（有时甚至会搞丢备份），这种情况下使用程序手段处理就变得十分必要。  

ItemVoid 会在收集服务器上的各种各样的特殊物品（带有自定义名称或者Lores的），并将其存储在数据库中。  
您可以使用命令在数据库中查找这些物品，并取出。

## 使用方法

/itemvoid queryName <名称> - 按名称搜索  
/itemvoid queryLore <名称> - 按Lores搜索  
/itemvoid queryLoreFullText <名称> - 按Lores搜索（使用全文索引）
