# M3Stage Architecture & Domain Metamodel Memory

## 1. Core Architectural Objectives
- **Compile-Time Safety & Exhaustiveness:** Replace loose string identifiers and dynamic maps with Kotlin sealed types and exhaustive enums. Eliminating runtime null checks (`?: error(...)`) and ensuring all `when` expressions are checked by the Kotlin compiler.
- **Decouple UI Taxonomy from Classpath Resolution:** Separate human-facing palette categories (`ComponentCategory`) from physical Compose package roots (`ComposeModule`).
- **Material 3 Design Token Integrity:** Retain dynamic theming semantics (`MaterialTheme.colorScheme.primary`, `typography.bodyMedium`, `shape.medium`) rather than emitting raw hex or literal values.
- **Atomic Design Architecture (Composition over Prop Flattening):**
  - **Atoms:** Primitives like `Text` (owns string, typography, color), `Icon` (owns imageVector, tint), `Image`, `Spacer`.
  - **Molecules:** Interactive units composed of atoms, e.g. `Button` containing `[Icon?, Text]`, `IconButton` containing `[Icon]`, `TextField`.
  - **Organisms:** Structural blocks like `Card` containing a Column of text and button rows, `TopAppBar`, `NavigationBar`.
  - **Templates/Pages:** High-level arrangement skeletons like `Scaffold` and `Screen`.
  - *Benefit:* Eliminates property duplication (e.g. `Button` does not duplicate text properties; it literally holds a `Text` atom child). Codegen matches handwritten Jetpack Compose 1:1.
- **Lean AST & Anti-Wrapper Invariant:**
  - Keep a **single unified `DesignNode` class** discriminated by an exhaustive `ComponentKind` enum. Do NOT create 50 separate `XNode` classes.
  - Rely on Kotlin primitives (`String`, `Float`, `Boolean`, `ULong`) and official M3 token enums (`M3ColorToken`, etc.) without custom proprietary wrappers.
- **M3 Component Anatomy & Named Slots:** Support designated slots (e.g., `TopAppBar` title/actions, `Scaffold` topBar/content) using strongly-typed slot tokens with strict child constraints.
- **Zero-Size Empty Drop Prevention:**
  - **Tier 1 (Drop Time):** Components instantiate with canonical default template anatomy (e.g. Button drops pre-populated with a Text atom; Card drops with Column and Text children).
  - **Tier 2 (Canvas / Empty State - [x] Completed):** `EmptyContainerPlaceholder.kt` renders interactive dashed-border drop zones with full width and min-height for empty containers (`Column`, `Row`, `Box`, `Card`), keeping layout wireframes visible and selectable without polluting generated code.
- **Device Viewport & Canvas Metamodel (Industry Standard Option 2):**
  - **Anti-String Literal Trap Invariant:** NO loose strings (e.g. `"pixel_8"`) for device selection. Represent devices with an exhaustive, strongly-typed `DevicePreset` enum (e.g. `Pixel8`, `Pixel8Pro`, `GalaxyS24`, `PixelFold`, `PixelTablet`, `Desktop`).
  - **Project Default with Screen-Level Override:** `Project` holds a default `DevicePreset`. `Screen` holds an optional `DevicePreset?` (null = inherit project default).
  - **Decoupled Responsibilities:** `:domain` owns the typed `DevicePreset` enum and viewport dimensions (`widthDp`, `heightDp`). `:desktopApp` owns the hardware visual frame presentation (bezel, notch/punch-hole, gesture pill, orientation toggle, zoom).

---

## 2. Industry-Standard Package Structure (`domain`)
Base package: `io.github.chandu4221.m3stage`

```
domain/src/main/kotlin/io/github/chandu4221/m3stage/
├── model/         # Document & AST Entities (Project, Screen, DesignNode, ModifierNode, NodeId)
├── component/     # Metamodel, Catalog & Classpath (ComponentKind, ComponentCategory, ComposeModule, ComponentDefinition, ComponentCatalog)
├── property/      # Property Engine & UI Descriptors (PropertyKey, PropertyValue, PropertyDescriptor, PropertyGroup, LayoutOptions)
├── theme/         # M3 Design Tokens (M3ColorToken, M3TypographyToken, M3ShapeToken)
├── mutation/      # Pure Immutable Tree Updates (NodeMutations, ProjectMutations)
├── query/         # Read-only Tree Traversal & Search (NodeQueries, ProjectQueries)
├── validation/    # Domain Invariants (ProjectValidation)
└── port/          # Driven Ports / SPI (CodeGenerator, ProjectRepository, IdGenerator)
```

---

## 3. Progress Tracking & Refactoring Ladder

### [x] Level 1: `theme/` (Completed)
- `M3ColorToken.kt`: Full 36-role Material 3 ColorScheme tokens matching official Material Theme Builder (Primary, Secondary, Tertiary, Surfaces, modern Surface Containers, Error, Outline, Scrim).
- `M3TypographyToken.kt`: 15 canonical M3 Typography scale roles (Display, Headline, Title, Body, Label).
- `M3ShapeToken.kt`: 7 canonical M3 Shape scale roles (None, ExtraSmall, Small, Medium, Large, ExtraLarge, Full).
- Connected into `model/PropValue.kt`.

### [x] Level 2: `property/` (Completed)
- `PropertyId.kt` / `PropertyKey.kt`: Strongly-typed phantom keys (`PropertyId`, `PropertyKey<V>`).
- `PropertyValue.kt`: Sum type representation of stored values (`StringValue`, `DpValue`, `ColorValue.Token`, `TypographyValue.Token`, `ShapeValue.Token`, etc.).
- `LayoutOptions.kt`: Explicit domain enums for Dropdowns (`HorizontalArrangementOption`, `VerticalArrangementOption`, `HorizontalAlignmentOption`, `VerticalAlignmentOption`, `ContentAlignmentOption`, `TextOverflowOption`).
- `PropertyGroup.kt`: Inspector groups in PascalCase (`Content`, `Layout`, `Appearance`, `Typography`, `Behavior`).
- `PropertyDescriptor.kt`: Inspector UI descriptors (`Text`, `Switch`, `DpSlider`, `EnumDropdown`, `ColorPicker`, `TypographyPicker`, `ShapePicker`).

### [x] Level 3: `component/` (Completed)
- `ComposeModule.kt`: Classpath roots (`Material3`, `FoundationLayout`, `Foundation`, `Ui`).
- `ComponentCategory.kt`: Palette categories in PascalCase (`Layouts`, `Surfaces`, `Inputs`, `Display`, `Navigation`).
- `ComponentKind.kt`: Exhaustive enum of all supported components (`Text`, `Icon`, `Image`, `Button`, `TextField`, `Column`, `Row`, `Box`, `Scaffold`, `Card`, `TopAppBar`).
- `SlotDefinition.kt`: Anatomy & named slot rules (`SlotId`, `isRequired`, `maxChildren`, `allowedKinds`).
- `ComponentDefinition.kt`: Schema descriptor with default properties factory and Atomic child constraints (`allowedChildren`).
- `ComponentCatalog.kt`: Non-nullable $O(1)$ registry with typed keys (`TextProps`, `ButtonProps`, `ColumnProps`, etc.).

### [x] Level 4: `model/` (Completed)
- `Identifiers.kt`: Cleaned up to solely hold document aggregate identity types (`ProjectId`, `ScreenId`, `NodeId`).
- `ModifierNode.kt`: Refactored to reference `PropertyValue.ColorValue` and `PropertyValue.ShapeValue`.
- `DesignNode.kt`: Strongly typed AST root with `kind: ComponentKind`, `props: Map<PropertyId, PropertyValue>`, and `slots: Map<SlotId, List<DesignNode>>`.

### [x] Level 5: `mutation/` & `validation/` (Completed)
- `NodeMutation.kt`: Migrated to `PropertyId`, `PropertyKey`, and `PropertyValue`.
- `ProjectValidation.kt`: Migrated to `child.kind.id`.
- Legacy files in `model/` safely deleted.

### [x] Level 6: `:adapter:compose-codegen` (Completed)
- Package reorganization into `adapter.codegen.component` (`BoxCodegen`, `ButtonCodegen`, `CardCodegen`, `ColumnCodegen`, `RowCodegen`, `TextCodegen`).
- Centralized `ComposeSymbols.kt` with KotlinPoet `%M` MemberNames (e.g., `androidx.compose.ui.unit.dp`) and `%T` ClassNames.
- `PackageNameResolver.kt` delegates directly to `kind.module.packageName`.
- `ComponentCodegen` and `ComposeCodeGenerator` updated to consume typed `ComponentKind`, `PropertyKey`, `PropertyValue`, and `slots`.

### [x] Level 7: `:adapter:json-persistence` (Completed)
- Clean split between DTO definitions in `dto/ProjectDto.kt` and domain conversion functions in `dto/Mappers.kt`.
- `ProjectDto.kt`: Pure `@Serializable` DTOs (`ProjectDto`, `ScreenDto`, `DesignNodeDto`, `PropValDto`, `ModifierNodeDto`) supporting named `slots`.
- `Mappers.kt`: Exhaustive, type-safe bidirectional mapping between domain entities (`DesignNode`, `PropertyValue`, `ModifierNode`, `M3ColorToken`) and DTOs.
- Resilient mapping of legacy or stored string IDs to `ComponentKind` via `ComponentKind.entries.firstOrNull { it.id == this.type } ?: ComponentKind.Box`.

### [x] Level 8: `:adapter:compose-renderer` (Completed)
- Subpackage reorganization into `adapter.renderer.component` (`BoxRenderer`, `ButtonRenderer`, `CardRenderer`, `ColumnRenderer`, `RowRenderer`, `TextRenderer`).
- New `ThemeResolver.kt` cleanly resolves domain tokens (`M3ColorToken`, `M3TypographyToken`, `M3ShapeToken`) into runtime Compose `MaterialTheme` color schemes, text styles, and shapes.
- `DesignRenderer.kt` refactored to use `Map<ComponentKind, NodeRenderer>` and route via `currentNode.kind`.
- `ButtonRenderer.kt` updated to follow Atomic Design (molecule rendering child atoms with container/content colors and enabled state).
- All 10 files compile and verify cleanly.

---

## 4. Current State: Domain & Adapters Status
- `:domain`: 100% verified, fully typed, compile-safe.
- `:adapter:compose-codegen`: 100% verified, subpackaged, compiles cleanly.
- `:adapter:json-persistence`: 100% verified, split DTOs/Mappers, compiles cleanly.
### [x] Level 9: `:desktopApp` UI Integration (Completed)
- `ProjectSessionDelegate.kt`: Root genesis uses `ComponentKind.Column` with strongly typed default properties from `ComponentCatalog`.
- `EditorStore.kt`: Bridges `addNodeToActiveScreen(parentId, kind: ComponentKind)`. Implements Atomic Design genesis (e.g. dropping a Button automatically pre-populates a child `Text` atom).
- `ComponentsPalettePanel.kt`: Collapsible categories iterate dynamically over `ComponentCatalog.groupedByCategory` (`category.displayName`), adding components via `definition.kind`.
- `InspectorPanel.kt`: Cleaned up to display `node.kind.displayName`.
- `EditorCommand.kt`: Migrated property commands to `PropertyId` and `PropertyValue`. Removed unused empty command stubs.

### [x] Level 10: 2-Tier Top Project App Bar & Screen Management (Completed)
- `TopProjectAppBar.kt`: 2-tier design with Tier 1 (branding, project title, Add Screen, Export Screens) and Tier 2 (scrollable LazyRow with screen pills and active screen auto-scroll).
- `AddScreenCommand` and `store.addNewScreen(name, route, device)` with full Undo/Redo integration.
- Attached Artboard Controls in `CanvasPanel.kt`: Screen title, Device preset dropdown, Orientation toggle, Bezel frame toggle, Viewport dimensions badge.

---

## 4. Current State: Complete End-to-End Metamodel Architecture Verification
- `:domain`: 100% verified, fully typed, compile-safe.
- `:adapter:compose-codegen`: 100% verified, subpackaged, compiles cleanly.
- `:adapter:json-persistence`: 100% verified, split DTOs/Mappers, compiles cleanly.
- `:adapter:compose-renderer`: 100% verified, subpackaged with `ThemeResolver`, compiles cleanly.
- `:desktopApp`: 100% verified, multi-screen workspace with interactive device frame and attached controls.
- Full build check: `./gradlew check` **BUILD SUCCESSFUL**.



