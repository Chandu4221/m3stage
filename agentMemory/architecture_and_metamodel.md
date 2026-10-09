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
- New `ThemeCodeResolver.kt` cleanly resolves domain tokens (`M3ColorToken`, `M3TypographyToken`, `M3ShapeToken`) into runtime Compose `MaterialTheme` color schemes, text styles, and shapes.
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

### [x] Level 11: Dynamic M3 Theming with MaterialKolor (Completed)
- Integrated `com.materialkolor:material-kolor:5.0.2` (Google M3 color utilities port for Kotlin Multiplatform).
- Metamodel updated: `Project` holds `seedColor: Long` (default Baseline Purple `0xFF6750A4`) and `isDarkMode: Boolean`.
- DTO & Persistence: `ProjectDto` and `Mappers.kt` serialize/deserialize `seedColor` and `isDarkMode` with backward-compatible defaults.
- Commands & Store: `UpdateThemeCommand` added to undo/redo history; `updateSeedColor` and `toggleDarkMode` exposed on `EditorStore`.
- Palette Presets: `SeedColorPresets.kt` defined with Baseline Purple, Emerald Green, Indigo, Amber, Sunset Coral, Deep Rose, etc.
- Unified Shell & Canvas: `DynamicMaterialTheme` in `main.kt` drives the entire desktop app and device canvas dynamically from the active project's seed color and dark mode state.
- Top Project App Bar: Seed color dropdown swatch picker + dark mode toggle switch added to Tier 1 controls.

---

### [x] Level 12: Comprehensive WCAG a11y & Dynamic Token Auditing (Completed)
- Eradicated all hardcoded hex colors from canvas backgrounds, bezels, and overlays.
- Canvas Studio: Migrated to dynamic `MaterialTheme.colorScheme.surfaceDim` adapting comfortably across both Dark and Light modes.
- Hardware Bezel & Gesture Pill: Uses dynamic `surfaceContainerLowest` and `onSurface.copy(alpha = 0.5f)`.
- Empty Container Placeholder: Uses M3 `outline` dashed border and `onSurfaceVariant` label text on `surfaceContainerHighest`, guaranteeing WCAG 2.1 AA 4.5:1 text contrast.
- Selection Overlay: Uses `MaterialTheme.colorScheme.tertiary` for locked items and `MaterialTheme.colorScheme.primary` for selected items; added `Role.Button` semantics.
- Screen Navigation Tabs: Migrated to Material 3 `FilterChip` with built-in accessibility roles (`Role.Tab`), active state announcements, and proper touch target dimensions.
- Touch Targets: Expanded dark mode toggle and interactive icons to standard 48×48 dp touch boundaries.

---

### [x] Level 13: Atomic Design for `:desktopApp` UI (100% Dumb Presentational Architecture) (Completed)
- **Principle**: Pure Presentational UI with Unidirectional Data Flow (UDF).
  - Atoms, Molecules, Organisms, and Templates are **100% dumb**:
    - No direct references to `EditorStore` or coroutine scopes.
    - Pure inputs (`data class` / primitives / enums) and pure output callbacks (`() -> Unit`, `(T) -> Unit`).
    - Zero business logic side-effects.
  - Single Smart Boundary: `EditorScreen.kt` (Page layer) connects `EditorStore` flows to `EditorShellTemplate`.
- **Package Hierarchy**:
  - `ui.atom`: `ToolIconButton`, `ColorSwatch`, `DotMatrixCell`, `SearchTextField`, `DimensionBadge`.
  - `ui.molecule`: `CapsuleToolbar`, `SegmentedButtonGroup`, `AlignmentMatrix`, `ComponentTile`.
  - `ui.organism`: `StudioNavRail`, `PartsDrawer`, `FloatingCanvasStudio`.
  - `ui.template`: `EditorShellTemplate` (assembles NavRail + Parts Drawer + Floating Canvas Studio + Inspector).
  - `ui.preview`: `PreviewTheme.kt` with `DualThemePreview` container rendering both Light & Dark mode previews side-by-side with dynamic `MaterialKolor`.
  - `ui.page`: `EditorScreen` (smart mediator wiring `EditorStore` to template).

---

### [x] Level 14: Multi-Screen Side-by-Side Artboards on Canvas (Completed)
- Infinite studio workbench with horizontal & vertical panning (`CanvasPanel`).
- Renders all screens in the project side-by-side in a horizontal artboard sequence (`48.dp` spacing).
- Each artboard has dedicated floating controls (Device preset dropdown, orientation toggle, hardware frame switch, dimension badge).
- Active screen auto-focus: Clicking any screen or component on the canvas activates that screen (`activeScreenId`), highlighting its artboard header with `primaryContainer` and accent border.
- Integrated "+ Add Screen" ghost artboard card at the end of the canvas row for instant screen creation directly on the workbench.
- 100% compile-safe, verified via `./gradlew check`.

---

### [x] Level 15: Component Registry Expansion (Icon, Image, TextField) (Completed)
- Added `IconProps`, `ImageProps`, and `TextFieldProps` to `ComponentCatalog`.
- Added `Icon`, `Image`, and `TextField` definitions with strongly typed descriptors to `ComponentCatalog.all`.
- Implemented `IconRenderer`, `ImageRenderer`, and `TextFieldRenderer` in `:adapter:compose-renderer`.
- Implemented `IconCodegen`, `ImageCodegen`, and `TextFieldCodegen` in `:adapter:compose-codegen`.
- Added graceful fallback handling in `ComposeCodeGenerator` to emit informative code comments instead of crashing with `error("Unregistered...")`.
- Mapped vector icons for all component kinds in `PartsDrawer.kt`.
- Verified 100% compile-safe across all 5 modules via `./gradlew check`.

### [x] Level 16: Canonical M3 Scaffold & TopAppBar Integration (Completed)
- Added `ScaffoldProps` (`ContainerColor`) and `TopAppBarProps` (`Title`, `ContainerColor`, `TitleContentColor`) to `ComponentCatalog`.
- Added `Scaffold` and `TopAppBar` definitions to `ComponentCatalog.all` (total 11 components, 100% registry coverage matching `ComponentKind`).
- Implemented `TopAppBarRenderer` and `ScaffoldRenderer` in `:adapter:compose-renderer`.
- Implemented `TopAppBarCodegen` and `ScaffoldCodegen` in `:adapter:compose-codegen` with `@OptIn(ExperimentalMaterial3Api::class)` screen export support.
- Adopted compositional children model (`Scaffold` inspects `children` for `TopAppBar` and content containers such as `Column`).
- Established `Scaffold` (with child `TopAppBar` and child `Column`) as the default root for all newly created screens in `ProjectSessionDelegate.createNewProject()` and `EditorStore.addNewScreen()`.
- Smart drop redirection in `EditorStore.addNodeToActiveScreen()` automatically routes non-TopAppBar additions into the Scaffold's main content column.
- Verified 100% compile-safe across all 5 modules via `./gradlew check`.

### [x] Level 17: WYSIWYG Inspector-to-Codegen Parity (Completed)
- Expanded `ComposeSymbols` with `MaterialTheme`, `Color`, `Alignment`, `Arrangement`, `RoundedCornerShape`, `TextOverflow`, `TopAppBarDefaults`.
- Implemented `ThemeCodeResolver` in `:adapter:compose-codegen` to convert `M3ColorToken`, hex colors, `M3TypographyToken`, and `M3ShapeToken` to idiomatic Compose KotlinPoet expressions.
- Upgraded all 11 component codegens with 100% property fidelity:
  - `Text`: exports `color`, `style`, and `overflow` (Ellipsis).
  - `Icon`: exports `tint`.
  - `Image`: exports `contentDescription`.
  - `Button`: exports `ButtonDefaults.buttonColors(containerColor, contentColor)`.
  - `TextField`: exports `label`, `placeholder`, `singleLine`.
  - `Column`: exports `horizontalAlignment` and `verticalArrangement` (`Arrangement.spacedBy` or positioning).
  - `Row`: exports `horizontalArrangement` (`Arrangement.spacedBy` or positioning) and `verticalAlignment`.
  - `Box`: exports `contentAlignment`.
  - `Card`: exports `elevation`, `CardDefaults.cardColors(containerColor)`, and `shape`.
  - `Scaffold`: exports `containerColor`.
  - `TopAppBar`: exports `TopAppBarDefaults.topAppBarColors(containerColor, titleContentColor)`.
- Verified 100% compile-safe across all 5 modules via `./gradlew check`.

### [x] Level 18: Kotlin Identifier Sanitization in Codegen (Completed)
- Implemented `KotlinIdentifierSanitizer` in `:adapter:compose-codegen`.
- Converts arbitrary user-facing screen names (e.g. `"Screen 1"`, `"my-profile"`) to safe, idiomatic PascalCase.
- Correctly prefixes identifiers starting with numbers with `"Screen"` (e.g. `"1Home"` $\rightarrow$ `"Screen1Home"`).
- Automatically avoids redundant double suffixes (e.g. `"HomeScreen"` remains `HomeScreen`, while `"Home"` becomes `HomeScreen`).
- Deduplicates screen names across project screens (e.g. `"Screen 1"` and duplicate `"Screen 1"` become `"Screen1.kt"` and `"Screen1_1.kt"`).
- Verified 100% compile-safe across all 5 modules via `./gradlew check`.

### [x] Level 19: Comprehensive Unit Test Suite & Round-Trip Invariance (Completed)
- `:domain`:
  - `ProjectValidationTest`: Verifies invariants (empty screen enforcement, duplicate Node ID detection across nested subtrees, valid project passing).
  - `DesignNodeOperationsTest`: Verifies immutable node tree lookups (`findNode`, `findParent`, `pathIdsTo`) and tree mutations (`addNode`, `removeNode`, `updateProp`).
  - `ComponentCatalogTest`: Verifies 100% registration of all 11 `ComponentKind` definitions and default props generator.
- `:adapter:compose-codegen`:
  - `KotlinIdentifierSanitizerTest`: Verifies PascalCase generation, leading digit prefixing, deduplication, and function naming.
- `:adapter:json-persistence`:
  - `SerializationRoundTripTest`: Proves end-to-end data fidelity: `Project` -> `ProjectDto` -> `JSON string` -> `ProjectDto` -> `Project` round-trip equality across all 11 component types and property tokens.
- Standardized test runners to `useJUnit()` across all subprojects.
- All unit tests pass cleanly via `./gradlew test` and `./gradlew check`.

### [x] Level 20: Resilient Persistence, Quarantine Backups & Code Hygiene (Completed)
- `:adapter:json-persistence`:
  - Added `CorruptProjectException` and automatic file quarantine backup: corrupted project files are preserved as `project.json.corrupt_<timestamp>` rather than silently dropped and overwritten on auto-save.
  - Added unit test `JsonProjectRepositoryTest` validating that corrupt files trigger quarantine backups.
- `:desktopApp`:
  - Enriched `EditorEvent.LoadFailed(reason)` with typed diagnostic error messages.
  - Updated `ProjectSessionDelegate.loadProject()` to surface validation and deserialization errors instead of creating blank projects.
- `:adapter:compose-renderer`:
  - Unified `currentNode.id == selectedNodeId` value class comparison in `DesignRenderer.kt`.
- `:adapter:compose-codegen`:
  - Added diagnostic stderr logging during ktfmt formatting fallback in `ComposeCodeGenerator.kt`.
- Verified 100% test & build pass across all modules via `./gradlew test` and `./gradlew check`.

### [x] Level 21: Store Lifecycle & Coroutine Dispatcher Optimization (Completed)
- `:desktopApp`:
  - `EditorStore` implements `AutoCloseable` with `close()` cancelling `storeScope` and `ProjectSessionDelegate.close()`.
  - Hooked `store.close()` to `Window(onCloseRequest)` in `main.kt` ensuring clean teardown of active coroutines.
  - Offloaded CPU-heavy code generation (KotlinPoet AST parsing & ktfmt Google Java/Kotlin formatting) to `Dispatchers.Default` (background worker pool) in `ProjectSessionDelegate.exportCode()`.
  - Offloaded file disk I/O in code export to `Dispatchers.IO`.
  - Desktop UI thread (`Dispatchers.Main` / Swing EDT) never blocks, ensuring smooth 60 FPS interactions.
- Full test and build check: `./gradlew check` **BUILD SUCCESSFUL**.

---

## 4. Current State: Complete End-to-End Metamodel Architecture Verification
- `:domain`: 100% verified, fully typed, compile-safe with all 11 component definitions and comprehensive unit test coverage.
- `:adapter:compose-codegen`: 100% verified, 11 components with full WYSIWYG Inspector parity, KotlinPoet, identifier sanitization, and unit tests.
- `:adapter:json-persistence`: 100% verified, split DTOs/Mappers, tested with round-trip invariance and corrupt file quarantine.
- `:adapter:compose-renderer`: 100% verified, 11 components with `ThemeResolver` and `EmptyContainerPlaceholder`.
- `:desktopApp`: Fully migrated to Dumb Atomic Design with Multi-Screen Artboard Canvas, 2-column Parts drawer, Studio NavRail, canonical `Scaffold` screen genesis, resilient error recovery, and background coroutine offloading.
- Full test and build check: `./gradlew check` **BUILD SUCCESSFUL**.





