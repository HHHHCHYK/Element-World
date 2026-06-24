# ElementWorld 代码规范审计记录

日期：2026-06-24  
范围：Fabric Minecraft Mod 项目 `Element-World`

## 背景

本次工作来自一次全项目代码规范性检查。检查目标不是只列问题，而是把确定性问题直接修复，并补上一个可以长期运行的轻量质量门禁，避免同类问题反复出现。

执行过程中，标准 `apply_patch` 通道在 Windows 沙箱助手处持续卡住/被取消，因此实际文件写入改用受控 PowerShell 完成；所有写入都限制在项目工作区内，并通过 Gradle 构建验证。

## 初始发现

主要问题包括：

- Fabric 示例模板残留：`Hello Fabric world!`、示例 `fabric.mod.json` 描述、示例 mixin、示例命令和测试输出类。
- 调试输出散落：`System.out.println`、硬编码命令提示、旧调试注释。
- Java 命名不规范：`Calculater`、`Electro_Charged`、`modifierType`、`modifierMethod`、`DAMAGE_TYPE`、`com.item` 包名等。
- 客户端 HUD 问题：网络接收器在 HUD 渲染回调中注册，等价于每帧重复注册；同时引用了不存在的 `textures/element/*.png` 资源。
- 容器实现问题：`BonusContainer` 懒加载集合创建后没有回写字段，可能导致增伤数据丢失。
- Mixin 伤害修改问题：`LivingEntityMixin` 计算了新的伤害值，但没有把结果写回 `ModifyArgs`。
- 项目缺少规范门禁：没有 Checkstyle、Spotless、PMD 或自定义检查任务。

## 已完成修复

### 项目元数据与模板清理

- 清理 `fabric.mod.json` 中的模板描述、作者、示例来源链接和无意义建议依赖。
- 删除示例 mixin、示例 Hello 命令、测试输出类等模板文件。
- 统一 mixin JSON 缩进。
- 补充并整理中英文语言文件中的命令反馈、死亡提示和物品使用提示。

### 命名与包结构规范化

- `Calculater` 改为 `Calculator`。
- `Electro_Charged` 改为 `ElectroCharged`。
- `com.item.GuiStick` 迁移到 `com.elementworld.item.GuiStick`。
- `ArgumentType` 注册类改为 `ModArgumentTypes`。
- `modifierType` / `modifierMethod` 改为 `ModifierType` / `ModifierMethod`。
- `DAMAGE_TYPE` 改为 `DamageKind`，枚举项改成大写语义化名称。
- `Element.ToEP` 改为 `Element.toEpClass`。
- `ElementType` 中的混合大小写枚举项改为全大写。

### 客户端 HUD 修复

- 将元素网络包接收器移动到客户端初始化路径，只注册一次。
- 将客户端元素列表改为私有静态状态，并在客户端线程中更新。
- 移除不存在的元素贴图引用，改为使用稳定色块绘制 HUD，避免资源缺失导致渲染问题。
- 新增公共 packet id 常量，避免服务端和客户端各自硬编码 `element_types`。

### 元素、反应和伤害组件整理

- 重写 `Reaction` 工厂方法，去掉无意义 `default -> null`，并统一常量命名。
- 修正反应构造参数拼写错误：`firseElement` / `secondELement` 改为 `firstElement` / `secondElement`。
- 将 `Reaction.damageValue` 从公共字段改为受保护字段。
- 简化 `Element` 工厂方法和枚举转换逻辑。
- 简化 `EWDamageSource`，移除无意义的 try/catch 和布尔返回式 setter。

### 容器和命令整理

- 将 `BonusContainer` 和 `ResistancesContainer` 改为基于 `Map` 的结构，减少重复字段和分支。
- 修复 `BonusContainer` 懒加载集合不回写导致的数据丢失问题。
- 将重复实例提示从 `System.out.println` 改为 logger warn。
- 重构 `/element` 命令构建逻辑，拆出 `listCommand` 和 `addCommand`，降低括号嵌套和维护成本。
- 命令反馈改为 `Text.translatable`，避免硬编码文本散落在 Java 代码中。
- `ElementTypeArgumentType` 增加非法元素输入的 Brigadier 异常提示。

### Mixin 与行为修复

- `LivingEntityMixin.elementContainer` 改为私有字段。
- 移除旧的注释式调试输出。
- 在伤害计算结束后调用 `args.set(1, amount)`，确保修改后的伤害值真正生效。

### 质量门禁

在 `build.gradle` 中新增 `codeStyleCheck`，并挂载到 `check`：

- 禁止 `System.out`。
- 禁止 `printStackTrace(`。
- 禁止 Fabric 示例模板文本回潮。
- 禁止 Java 通配符导入。
- 禁止 Java 源文件名包含下划线。

## 验证结果

已执行并通过：

```powershell
.\gradlew.bat check --console=plain
.\gradlew.bat build --console=plain
```

最终完整构建结果：`BUILD SUCCESSFUL`。

另外执行过：

```powershell
git diff --check
```

结果无尾随空格等硬格式错误；Git 仍提示若干工作区文件下一次被 Git 触碰时 CRLF 会按 `.gitattributes` 归一化为 LF。

## 当前注意事项

- 本次改动范围较大，主要集中在规范化和低风险缺陷修复。后续建议在提交前人工 review 一遍核心战斗逻辑路径。
- 目前项目仍没有单元测试源码，Gradle 显示 `test NO-SOURCE`。后续可以优先为元素反应工厂、伤害乘区和容器增减逻辑补测试。
- `ElementContainer` 和 `LivingEntityMixin` 仍然偏大，后续可以继续拆分，但本次没有做大架构迁移，以免扩大行为风险。
- 标准 `apply_patch` 通道在本次会话中仍不稳定；后续若沙箱恢复，建议重新使用 `apply_patch` 作为默认编辑方式。