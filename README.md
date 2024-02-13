# ItemVoid

物品NBT漏斗，收纳一切！

## 依赖

* [NBTAPI](https://modrinth.com/plugin/nbtapi)
* 一台强力的 MySQL 服务器 (MySQL 5.7 或者更高版本)

## 描述

在 RIA，整个服务器由大量的自定义物品驱动。这些物品的数量实在是太多，即使建设了多个仓库，也相当难以查找对应物品（有时甚至会搞丢备份），这种情况下使用程序手段处理就变得十分必要。  

ItemVoid 会在收集服务器上的各种各样的特殊物品（带有自定义名称或者Lores的），并将其存储在数据库中。  
您可以使用命令在数据库中查找这些物品，并取出。

<img width="813" alt="3246aeceaa51c96baed42e69756c4f0c" src="https://github.com/RIA-AED/ItemVoid/assets/30802565/129a4b19-d953-4169-a722-29dadfae9834">  

<img width="813" alt="3246aeceaa51c96baed42e69756c4f0c" src="https://github.com/RIA-AED/ItemVoid/assets/30802565/8f5e5caf-43ba-424d-8d11-3ced40f0965c">  


## 使用方法

/itemvoid queryName <名称> - 按名称搜索  
/itemvoid queryLore <名称> - 按名称搜索  
/itemvoid queryNameFullText <名称> - 按名称搜索（使用全文索引）  
/itemvoid queryLoreFullText <名称> - 按名称搜索（使用全文索引）  
/itemvoid queryEverything <名称> - 按名称和Lore搜索（使用全文索引）  

## 什么是全文索引

使用全文搜索需要关键字至少有 4 个字符。
好处是：搜索速度快，对数据库压力小
坏处是：搜索不精确，有很多遗漏结果

## 什么时候该使用全文索引

如果查询的时候耗时很久，则可以考虑使用全文索引查询。
如果足够快，则推荐使用普通查询。
