package io.github.chandu4221.m3stage.adapter.codegen

import io.github.chandu4221.m3stage.adapter.codegen.resolver.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ModifierCodeResolver.requiredImport
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.ButtonDefinition
import io.github.chandu4221.m3stage.component.definition.CardDefinition
import io.github.chandu4221.m3stage.component.definition.FloatingActionButtonDefinition
import io.github.chandu4221.m3stage.component.definition.TextDefinition
import io.github.chandu4221.m3stage.model.*
import io.github.chandu4221.m3stage.modifier.ModifierScope
import io.github.chandu4221.m3stage.modifier.ModifierSpec
import io.github.chandu4221.m3stage.port.IdGenerator
import io.github.chandu4221.m3stage.property.PropertyValue
import io.github.chandu4221.m3stage.theme.M3ColorToken
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GeneratedCodeCompilesTest {

    @Test
    fun testAllModifierSpecsHaveRequiredImport() {
        val sampleModifiers = listOf(
            ModifierSpec.Padding.all(8f),
            ModifierSpec.FillMaxWidth(1f),
            ModifierSpec.FillMaxHeight(1f),
            ModifierSpec.FillMaxSize(1f),
            ModifierSpec.Size(24f, 24f),
            ModifierSpec.Width(40f),
            ModifierSpec.Height(40f),
            ModifierSpec.WrapContentSize(false),
            ModifierSpec.Weight(1f),
            ModifierSpec.AspectRatio(1.5f),
            ModifierSpec.Offset(4f, 4f),
            ModifierSpec.ZIndex(2f),
            ModifierSpec.Background(PropertyValue.ColorValue.Token(M3ColorToken.Primary)),
            ModifierSpec.Border(1f, PropertyValue.ColorValue.Token(M3ColorToken.Outline)),
            ModifierSpec.Shadow(4f),
            ModifierSpec.Clip(PropertyValue.ShapeValue.UniformDp(8f)),
            ModifierSpec.Alpha(0.8f),
            ModifierSpec.Rotate(90f),
            ModifierSpec.Scale(1.2f),
            ModifierSpec.VerticalScroll(true),
            ModifierSpec.HorizontalScroll(true)
        )

        val sealedSubclasses = ModifierSpec::class.sealedSubclasses
        val coveredClasses = sampleModifiers.map { it::class }.toSet()

        assertEquals(
            sealedSubclasses.toSet(),
            coveredClasses,
            "Sample list must cover all sealed subclasses of ModifierSpec"
        )

        for (spec in sampleModifiers) {
            val imp = spec.requiredImport()
            assertNotNull(imp, "ModifierSpec ${spec::class.simpleName} must have a non-null requiredImport")
            assertTrue(imp.isNotBlank(), "ModifierSpec ${spec::class.simpleName} requiredImport must not be blank")
        }
    }

    @Test
    fun testGeneratedScreenWithAllModifiersEmitsLegallyAndContainsRequiredImports() {
        val columnChild = DesignNode(
            id = NodeId("col-child"),
            kind = ComponentKind.Button,
            modifiers = listOf(
                ModifierSpec.Weight(1f, fill = true),
                ModifierSpec.Padding.all(8f),
                ModifierSpec.FillMaxWidth(1f),
                ModifierSpec.FillMaxHeight(1f),
                ModifierSpec.FillMaxSize(1f),
                ModifierSpec.Size(50f, 50f),
                ModifierSpec.Width(100f),
                ModifierSpec.Height(48f),
                ModifierSpec.WrapContentSize(true),
                ModifierSpec.AspectRatio(1.5f),
                ModifierSpec.Offset(2f, 4f),
                ModifierSpec.Background(PropertyValue.ColorValue.Token(M3ColorToken.Primary)),
                ModifierSpec.Border(1f, PropertyValue.ColorValue.Token(M3ColorToken.Outline)),
                ModifierSpec.Shadow(4f),
                ModifierSpec.Clip(PropertyValue.ShapeValue.UniformDp(8f)),
                ModifierSpec.Alpha(0.9f),
                ModifierSpec.Rotate(15f),
                ModifierSpec.Scale(1.1f),
                ModifierSpec.VerticalScroll(true),
                ModifierSpec.HorizontalScroll(true)
            )
        )

        val boxChild = DesignNode(
            id = NodeId("box-child"),
            kind = ComponentKind.Text,
            props = mapOf(
                TextDefinition.TextContent.id to PropertyValue.StringValue("ZIndex Inside Box")
            ),
            modifiers = listOf(
                ModifierSpec.ZIndex(3f)
            )
        )

        val box = DesignNode(
            id = NodeId("box-container"),
            kind = ComponentKind.Box,
            children = listOf(boxChild)
        )

        val column = DesignNode(
            id = NodeId("column-container"),
            kind = ComponentKind.Column,
            children = listOf(columnChild, box)
        )

        val project = Project(
            id = ProjectId("test-proj"),
            name = "Test App",
            basePackage = "com.m3stage.test",
            defaultDevice = DevicePreset.Pixel8,
            screens = listOf(
                Screen(
                    id = ScreenId("screen-1"),
                    name = "AllModifiersScreen",
                    route = "/all-modifiers",
                    root = column
                )
            )
        )

        val generator = ComposeCodeGenerator()
        val generatedProject = generator.generate(project)
        val file = generatedProject.files.first { it.fileName == "AllModifiersScreen.kt" }
        val code = file.content

        // 1. Assert required imports
        assertTrue(code.contains("import androidx.compose.foundation.layout"), "Must import layout")
        assertTrue(code.contains("import androidx.compose.ui.draw"), "Must import ui.draw")
        assertTrue(code.contains("import androidx.compose.foundation"), "Must import foundation")
        assertTrue(code.contains("import androidx.compose.ui"), "Must import ui")

        // 2. Assert scoped modifiers are emitted in appropriate contexts
        assertTrue(code.contains(".weight(1.0f)"), "Weight must be generated inside Column child")
        assertTrue(code.contains(".zIndex(3.0f)"), "zIndex must be generated inside Box child")
    }

    @Test
    fun testScopedModifiersAreSkippedWhenInIllegalParent() {
        val illegalWeightChild = DesignNode(
            id = NodeId("illegal-weight"),
            kind = ComponentKind.Button,
            modifiers = listOf(
                ModifierSpec.Weight(1f) // inside Box is illegal!
            )
        )

        val illegalZIndexChild = DesignNode(
            id = NodeId("illegal-zindex"),
            kind = ComponentKind.Text,
            props = mapOf(TextDefinition.TextContent.id to PropertyValue.StringValue("Hi")),
            modifiers = listOf(
                ModifierSpec.ZIndex(2f) // inside Column is illegal!
            )
        )

        val box = DesignNode(
            id = NodeId("box-parent"),
            kind = ComponentKind.Box,
            children = listOf(illegalWeightChild)
        )

        val column = DesignNode(
            id = NodeId("col-parent"),
            kind = ComponentKind.Column,
            children = listOf(illegalZIndexChild, box)
        )

        val project = Project(
            id = ProjectId("test-proj"),
            name = "Test App",
            basePackage = "com.m3stage.test",
            defaultDevice = DevicePreset.Pixel8,
            screens = listOf(
                Screen(
                    id = ScreenId("screen-1"),
                    name = "IllegalModifiersScreen",
                    route = "/illegal-modifiers",
                    root = column
                )
            )
        )

        val generator = ComposeCodeGenerator()
        val generated = generator.generate(project)
        val code = generated.files.first().content

        // Weight inside Box must be skipped
        assertTrue(!code.contains(".weight("), "Weight inside Box must be skipped defensively")
        // ZIndex inside Column must be skipped
        assertTrue(!code.contains(".zIndex("), "ZIndex inside Column must be skipped defensively")
    }

    @Test
    fun testZeroModifiersEmitsNoModifierImports() {
        val rootText = DesignNode(
            id = NodeId("root-text"),
            kind = ComponentKind.Text,
            props = mapOf(TextDefinition.TextContent.id to PropertyValue.StringValue("Hello")),
            modifiers = emptyList()
        )

        val project = Project(
            id = ProjectId("test-proj"),
            name = "Test App",
            basePackage = "com.m3stage.test",
            defaultDevice = DevicePreset.Pixel8,
            screens = listOf(
                Screen(
                    id = ScreenId("screen-1"),
                    name = "ZeroModifiersScreen",
                    route = "/zero-modifiers",
                    root = rootText
                )
            )
        )

        val generator = ComposeCodeGenerator()
        val generated = generator.generate(project)
        val code = generated.files.first().content

        assertTrue(!code.contains("import androidx.compose.ui.Modifier"), "Must not import Modifier when no modifiers exist")
        assertTrue(!code.contains("import androidx.compose.foundation.layout"), "Must not import layout when no layout modifiers exist")
        assertTrue(!code.contains("import androidx.compose.ui.draw"), "Must not import draw when no draw modifiers exist")
    }

    @Test
    fun testAllDefaultSubtreesGenerateValidComposeCode() {
        val generator = ComposeCodeGenerator()
        var idCounter = 0
        val idGen = object : IdGenerator {
            override fun nextProjectId(): ProjectId = ProjectId("proj-${idCounter++}")
            override fun nextScreenId(): ScreenId = ScreenId("screen-${idCounter++}")
            override fun nextNodeId(): NodeId = NodeId("node-${idCounter++}")
        }

        for (def in ComponentCatalog.all) {
            val rootNode = if (def.kind == ComponentKind.NavigationBarItem) {
                DesignNode(
                    id = NodeId("row-container"),
                    kind = ComponentKind.Row,
                    children = listOf(def.createDefaultSubtree(idGen))
                )
            } else {
                def.createDefaultSubtree(idGen)
            }

            val screen = Screen(
                id = ScreenId("screen-${def.kind.name.lowercase()}"),
                name = "${def.kind.name}Screen",
                route = "/${def.kind.name.lowercase()}",
                root = rootNode
            )
            val project = Project(
                id = ProjectId("test-proj"),
                name = "Test App",
                basePackage = "com.m3stage.test",
                defaultDevice = DevicePreset.Pixel8,
                screens = listOf(screen)
            )

            val generatedProject = generator.generate(project)
            val file = generatedProject.files.first()
            val code = file.content

            assertTrue(code.isNotBlank(), "Generated code for ${def.kind} must not be blank")
            assertTrue(code.contains("@Composable"), "Generated code for ${def.kind} must contain @Composable")
            assertTrue(
                !code.contains("TODO: Unsupported component kind"),
                "Generated code for ${def.kind} contains unsupported component kind placeholder"
            )

            // Assert that the generated code parses as valid Kotlin with ktfmt (no ParseError)
            com.facebook.ktfmt.format.Formatter.format(
                com.facebook.ktfmt.format.Formatter.GOOGLE_FORMAT,
                code
            )
        }
    }

    @Test
    fun testScreenWithScaffoldAndAllComponentsGeneratesValidComposeCode() {
        val generator = ComposeCodeGenerator()
        var idCounter = 0
        val idGen = object : IdGenerator {
            override fun nextProjectId(): ProjectId = ProjectId("proj-${idCounter++}")
            override fun nextScreenId(): ScreenId = ScreenId("screen-${idCounter++}")
            override fun nextNodeId(): NodeId = NodeId("node-${idCounter++}")
        }

        val nonScreenKinds = ComponentCatalog.all
            .map { it.kind }
            .filter {
                it != ComponentKind.Scaffold &&
                it != ComponentKind.NavigationBarItem &&
                it != ComponentKind.TopAppBar &&
                it != ComponentKind.NavigationBar
            }

        val contentChildren = nonScreenKinds.map { kind ->
            ComponentCatalog[kind].createDefaultSubtree(idGen)
        }

        val column = DesignNode(
            id = NodeId("all-components-col"),
            kind = ComponentKind.Column,
            children = contentChildren
        )

        val scaffold = DesignNode(
            id = NodeId("all-scaffold"),
            kind = ComponentKind.Scaffold,
            slots = mapOf(
                ComponentCatalog.ScaffoldProps.TopBarSlot.id to listOf(
                    ComponentCatalog[ComponentKind.TopAppBar].createDefaultSubtree(idGen)
                ),
                ComponentCatalog.ScaffoldProps.BottomBarSlot.id to listOf(
                    ComponentCatalog[ComponentKind.NavigationBar].createDefaultSubtree(idGen)
                ),
                ComponentCatalog.ScaffoldProps.ContentSlot.id to listOf(column)
            )
        )

        val project = Project(
            id = ProjectId("test-proj"),
            name = "All In One App",
            basePackage = "com.m3stage.test",
            defaultDevice = DevicePreset.Pixel8,
            screens = listOf(
                Screen(
                    id = ScreenId("screen-all"),
                    name = "AllComponentsScreen",
                    route = "/all",
                    root = scaffold
                )
            )
        )

        val generated = generator.generate(project)
        val code = generated.files.first().content
        assertTrue(code.isNotBlank(), "All-in-one screen code must not be blank")
        assertTrue(!code.contains("TODO: Unsupported component kind"), "Generated code contains unsupported component kind")
        com.facebook.ktfmt.format.Formatter.format(
            com.facebook.ktfmt.format.Formatter.GOOGLE_FORMAT,
            code
        )
    }

    @Test
    fun testAdaptiveScreenWithNavigationBarGeneratesCompilableComposeCode() {
        val generator = ComposeCodeGenerator()
        var idCounter = 0
        val idGen = object : IdGenerator {
            override fun nextProjectId(): ProjectId = ProjectId("proj-${idCounter++}")
            override fun nextScreenId(): ScreenId = ScreenId("screen-${idCounter++}")
            override fun nextNodeId(): NodeId = NodeId("node-${idCounter++}")
        }

        val navBarSubtree = ComponentCatalog[ComponentKind.NavigationBar].createDefaultSubtree(idGen)
        val adaptiveScreen = Screen(
            id = ScreenId("screen-adaptive-nav"),
            name = "AdaptiveNavScreen",
            route = "/adaptive-nav",
            adaptive = true,
            root = navBarSubtree
        )

        val project = Project(
            id = ProjectId("test-proj-adaptive"),
            name = "Adaptive Nav App",
            basePackage = "com.m3stage.test",
            defaultDevice = DevicePreset.Pixel8,
            screens = listOf(adaptiveScreen)
        )

        val generated = generator.generate(project)
        val code = generated.files.first().content

        assertTrue(code.isNotBlank(), "Adaptive screen code must not be blank")
        assertTrue(
            code.contains("windowSizeClass: WindowSizeClass = WindowSizeClass.Compact"),
            "Adaptive screen must accept windowSizeClass parameter with Compact default. Code:\n$code"
        )
        assertTrue(
            code.contains("when (windowSizeClass)"),
            "Adaptive screen must contain when (windowSizeClass) switch. Code:\n$code"
        )
        assertTrue(
            code.contains("WindowSizeClass.Expanded ->"),
            "Adaptive screen must handle WindowSizeClass.Expanded branch. Code:\n$code"
        )
        assertTrue(
            code.contains("NavigationRail"),
            "Adaptive screen must emit NavigationRail in expanded branch. Code:\n$code"
        )
        assertTrue(
            code.contains("NavigationRailItem"),
            "Adaptive screen must emit NavigationRailItem in expanded branch. Code:\n$code"
        )
        assertTrue(
            code.contains("NavigationBar"),
            "Adaptive screen must emit NavigationBar in else branch. Code:\n$code"
        )
        assertTrue(
            code.contains("NavigationBarItem"),
            "Adaptive screen must emit NavigationBarItem in else branch. Code:\n$code"
        )
        assertTrue(
            code.contains("import io.github.chandu4221.m3stage.model.WindowSizeClass"),
            "Adaptive screen must import WindowSizeClass. Code:\n$code"
        )

        // Verify valid Kotlin syntax via ktfmt Google format parser
        com.facebook.ktfmt.format.Formatter.format(
            com.facebook.ktfmt.format.Formatter.GOOGLE_FORMAT,
            code
        )
    }

    @Test
    fun testButtonWithInheritedLabelColorGeneratesValidCodeWithoutColorArg() {
        val generator = ComposeCodeGenerator()
        var idCounter = 0
        val idGen = object : IdGenerator {
            override fun nextProjectId(): ProjectId = ProjectId("p-${idCounter++}")
            override fun nextScreenId(): ScreenId = ScreenId("s-${idCounter++}")
            override fun nextNodeId(): NodeId = NodeId("n-${idCounter++}")
        }

        val buttonSubtree = ComponentCatalog[ComponentKind.Button].createDefaultSubtree(idGen)
        val screen = Screen(
            id = ScreenId("screen-btn"),
            name = "ButtonScreen",
            route = "/btn",
            root = buttonSubtree
        )
        val project = Project(
            id = ProjectId("test-proj-btn"),
            name = "Button App",
            basePackage = "com.m3stage.test",
            defaultDevice = DevicePreset.Pixel8,
            screens = listOf(screen)
        )

        val generated = generator.generate(project)
        val code = generated.files.first().content

        assertTrue(code.contains("Button("), "Generated code must emit Button(")
        assertTrue(code.contains("Text("), "Generated code must emit Text(")
        // The Text inside Button has Inherit color, so it must NOT emit an explicit color parameter
        assertTrue(!code.contains("color = MaterialTheme.colorScheme.onSurface"), "Inherited label must NOT emit onSurface")
        // Formatter test
        com.facebook.ktfmt.format.Formatter.format(
            com.facebook.ktfmt.format.Formatter.GOOGLE_FORMAT,
            code
        )
    }

    @Test
    fun testIconWithGlyphGeneratesValidCodeWithMatchingVectorAndDescription() {
        val generator = ComposeCodeGenerator()
        var idCounter = 0
        val idGen = object : IdGenerator {
            override fun nextProjectId(): ProjectId = ProjectId("p-${idCounter++}")
            override fun nextScreenId(): ScreenId = ScreenId("s-${idCounter++}")
            override fun nextNodeId(): NodeId = NodeId("n-${idCounter++}")
        }

        val iconNode = DesignNode(
            id = idGen.nextNodeId(),
            kind = ComponentKind.Icon,
            props = mapOf(
                io.github.chandu4221.m3stage.component.definition.IconDefinition.Glyph.id to PropertyValue.EnumValue(IconGlyph.Search.name)
            )
        )
        val screen = Screen(
            id = ScreenId("screen-icon"),
            name = "IconScreen",
            route = "/icon",
            root = iconNode
        )
        val project = Project(
            id = ProjectId("test-proj-icon"),
            name = "Icon App",
            basePackage = "com.m3stage.test",
            defaultDevice = DevicePreset.Pixel8,
            screens = listOf(screen)
        )

        val generated = generator.generate(project)
        val code = generated.files.first().content

        assertTrue(
            code.contains("imageVector = androidx.compose.material.icons.Icons.Default.Search"),
            "Generated icon must reference Search icon. Code:\n$code"
        )
        assertTrue(
            code.contains("contentDescription = \"Search\""),
            "Generated icon must include accessible contentDescription. Code:\n$code"
        )
        com.facebook.ktfmt.format.Formatter.format(
            com.facebook.ktfmt.format.Formatter.GOOGLE_FORMAT,
            code
        )
    }

    @Test
    fun testNavigationBarPrefillGeneratesCompilableCodeWithFourDistinctGlyphs() {
        val generator = ComposeCodeGenerator()
        var idCounter = 0
        val idGen = object : IdGenerator {
            override fun nextProjectId(): ProjectId = ProjectId("p-${idCounter++}")
            override fun nextScreenId(): ScreenId = ScreenId("s-${idCounter++}")
            override fun nextNodeId(): NodeId = NodeId("n-${idCounter++}")
        }

        val navBarSubtree = ComponentCatalog[ComponentKind.NavigationBar].createDefaultSubtree(idGen)
        val screen = Screen(
            id = ScreenId("screen-nav"),
            name = "NavScreen",
            route = "/nav",
            root = navBarSubtree
        )
        val project = Project(
            id = ProjectId("test-proj-nav"),
            name = "Nav App",
            basePackage = "com.m3stage.test",
            defaultDevice = DevicePreset.Pixel8,
            screens = listOf(screen)
        )

        val generated = generator.generate(project)
        val code = generated.files.first().content

        assertTrue(code.contains("Icons.Default.Home"), "Code must contain Home icon")
        assertTrue(code.contains("Icons.Default.Search"), "Code must contain Search icon")
        assertTrue(code.contains("Icons.Default.Settings"), "Code must contain Settings icon")
        assertTrue(code.contains("Icons.Default.Person"), "Code must contain Person icon")
        assertTrue(code.contains("contentDescription = \"Home\""), "Code must contain Home description")
        assertTrue(code.contains("contentDescription = \"Search\""), "Code must contain Search description")

        com.facebook.ktfmt.format.Formatter.format(
            com.facebook.ktfmt.format.Formatter.GOOGLE_FORMAT,
            code
        )
    }

    @Test
    fun testCardWithUnsetColorGeneratesCodeWithoutColorsArgument() {
        val generator = ComposeCodeGenerator()
        var idCounter = 0
        val idGen = object : IdGenerator {
            override fun nextProjectId(): ProjectId = ProjectId("p-${idCounter++}")
            override fun nextScreenId(): ScreenId = ScreenId("s-${idCounter++}")
            override fun nextNodeId(): NodeId = NodeId("n-${idCounter++}")
        }

        val cardSubtree = ComponentCatalog[ComponentKind.Card].createDefaultSubtree(idGen)
        val screen = Screen(
            id = ScreenId("screen-card-unset"),
            name = "CardUnsetScreen",
            route = "/card-unset",
            root = cardSubtree
        )
        val project = Project(
            id = ProjectId("test-proj-card-unset"),
            name = "Card App",
            basePackage = "com.m3stage.test",
            defaultDevice = DevicePreset.Pixel8,
            screens = listOf(screen)
        )

        val generated = generator.generate(project)
        val code = generated.files.first().content

        assertTrue(code.contains("Card("), "Generated code must emit Card(")
        assertTrue(!code.contains("colors = "), "Unset card must NOT emit colors argument")
        assertTrue(!code.contains("cardColors"), "Unset card must NOT call cardColors")

        com.facebook.ktfmt.format.Formatter.format(
            com.facebook.ktfmt.format.Formatter.GOOGLE_FORMAT,
            code
        )
    }

    @Test
    fun testCardWithExplicitColorGeneratesColorsArgument() {
        val generator = ComposeCodeGenerator()
        val cardNode = DesignNode(
            id = NodeId("card-explicit"),
            kind = ComponentKind.Card,
            props = mapOf(
                CardDefinition.ContainerColor.id to PropertyValue.ColorValue.Token(M3ColorToken.PrimaryContainer)
            )
        )
        val screen = Screen(
            id = ScreenId("screen-card-explicit"),
            name = "CardExplicitScreen",
            route = "/card-explicit",
            root = cardNode
        )
        val project = Project(
            id = ProjectId("test-proj-card-explicit"),
            name = "Card App",
            basePackage = "com.m3stage.test",
            defaultDevice = DevicePreset.Pixel8,
            screens = listOf(screen)
        )

        val generated = generator.generate(project)
        val code = generated.files.first().content

        assertTrue(code.contains("Card("), "Generated code must emit Card(")
        assertTrue(
            code.contains("colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)"),
            "Explicit card color must emit CardDefaults.cardColors with primaryContainer. Code:\n$code"
        )

        com.facebook.ktfmt.format.Formatter.format(
            com.facebook.ktfmt.format.Formatter.GOOGLE_FORMAT,
            code
        )
    }

    @Test
    fun testTextWithOverflowGeneratesValidCode() {
        val generator = ComposeCodeGenerator()
        val textNode = DesignNode(
            id = NodeId("text-overflow"),
            kind = ComponentKind.Text,
            props = mapOf(
                TextDefinition.TextContent.id to PropertyValue.StringValue("Truncated Text"),
                TextDefinition.Overflow.id to PropertyValue.EnumValue(io.github.chandu4221.m3stage.property.TextOverflowOption.Ellipsis.name)
            )
        )
        val project = Project(
            id = ProjectId("test-proj-text-overflow"),
            name = "Text Overflow App",
            basePackage = "com.m3stage.test",
            defaultDevice = DevicePreset.Pixel8,
            screens = listOf(
                Screen(
                    id = ScreenId("screen-text-overflow"),
                    name = "TextOverflowScreen",
                    route = "/overflow",
                    root = textNode
                )
            )
        )

        val generated = generator.generate(project)
        val code = generated.files.first().content

        assertTrue(code.contains("Text("), "Generated code must emit Text(")
        assertTrue(code.contains("overflow = TextOverflow.Ellipsis"), "Code must contain overflow = TextOverflow.Ellipsis. Code:\n$code")
        com.facebook.ktfmt.format.Formatter.format(
            com.facebook.ktfmt.format.Formatter.GOOGLE_FORMAT,
            code
        )
    }

    @Test
    fun testTextFieldWithSlotsGeneratesValidCode() {
        val generator = ComposeCodeGenerator()
        val iconNode = DesignNode(
            id = NodeId("tf-icon"),
            kind = ComponentKind.Icon,
            props = mapOf(
                io.github.chandu4221.m3stage.component.definition.IconDefinition.Glyph.id to PropertyValue.EnumValue("Search")
            )
        )
        val supportNode = DesignNode(
            id = NodeId("tf-support"),
            kind = ComponentKind.Text,
            props = mapOf(
                TextDefinition.TextContent.id to PropertyValue.StringValue("Helper text")
            )
        )
        val textFieldNode = DesignNode(
            id = NodeId("tf-slots"),
            kind = ComponentKind.TextField,
            props = mapOf(
                io.github.chandu4221.m3stage.component.definition.TextFieldDefinition.Label.id to PropertyValue.StringValue("Search Field")
            ),
            slots = mapOf(
                io.github.chandu4221.m3stage.component.definition.TextFieldDefinition.LeadingIconSlot.id to listOf(iconNode),
                io.github.chandu4221.m3stage.component.definition.TextFieldDefinition.SupportingTextSlot.id to listOf(supportNode)
            )
        )
        val project = Project(
            id = ProjectId("test-proj-tf-slots"),
            name = "TF Slots App",
            basePackage = "com.m3stage.test",
            defaultDevice = DevicePreset.Pixel8,
            screens = listOf(
                Screen(
                    id = ScreenId("screen-tf-slots"),
                    name = "TfSlotsScreen",
                    route = "/tf-slots",
                    root = textFieldNode
                )
            )
        )

        val generated = generator.generate(project)
        val code = generated.files.first().content

        assertTrue(code.contains("TextField("), "Generated code must emit TextField(")
        assertTrue(code.contains("leadingIcon = {"), "Code must contain leadingIcon. Code:\n$code")
        assertTrue(code.contains("supportingText = {"), "Code must contain supportingText. Code:\n$code")
        com.facebook.ktfmt.format.Formatter.format(
            com.facebook.ktfmt.format.Formatter.GOOGLE_FORMAT,
            code
        )
    }

    @Test
    fun testColumnRowBoxAlignmentAndArrangementGenerateValidCode() {
        val generator = ComposeCodeGenerator()
        val boxNode = DesignNode(
            id = NodeId("test-box"),
            kind = ComponentKind.Box,
            props = mapOf(
                io.github.chandu4221.m3stage.component.definition.BoxDefinition.ContentAlignment.id to PropertyValue.EnumValue(io.github.chandu4221.m3stage.property.ContentAlignmentOption.Center.name)
            )
        )
        val rowNode = DesignNode(
            id = NodeId("test-row"),
            kind = ComponentKind.Row,
            props = mapOf(
                io.github.chandu4221.m3stage.component.definition.RowDefinition.HorizontalArrangement.id to PropertyValue.EnumValue(io.github.chandu4221.m3stage.property.HorizontalArrangementOption.SpaceBetween.name),
                io.github.chandu4221.m3stage.component.definition.RowDefinition.VerticalAlignment.id to PropertyValue.EnumValue(io.github.chandu4221.m3stage.property.VerticalAlignmentOption.CenterVertically.name)
            ),
            children = listOf(boxNode)
        )
        val colNode = DesignNode(
            id = NodeId("test-col"),
            kind = ComponentKind.Column,
            props = mapOf(
                io.github.chandu4221.m3stage.component.definition.ColumnDefinition.HorizontalAlignment.id to PropertyValue.EnumValue(io.github.chandu4221.m3stage.property.HorizontalAlignmentOption.CenterHorizontally.name),
                io.github.chandu4221.m3stage.component.definition.ColumnDefinition.VerticalArrangement.id to PropertyValue.EnumValue(io.github.chandu4221.m3stage.property.VerticalArrangementOption.SpaceAround.name)
            ),
            children = listOf(rowNode)
        )
        val project = Project(
            id = ProjectId("test-proj-layout-align"),
            name = "Layout Align App",
            basePackage = "com.m3stage.test",
            defaultDevice = DevicePreset.Pixel8,
            screens = listOf(
                Screen(
                    id = ScreenId("screen-layout-align"),
                    name = "LayoutAlignScreen",
                    route = "/layout-align",
                    root = colNode
                )
            )
        )

        val generated = generator.generate(project)
        val code = generated.files.first().content

        assertTrue(code.contains("contentAlignment = Alignment.Center"), "Box must emit contentAlignment. Code:\n$code")
        assertTrue(code.contains("horizontalAlignment = Alignment.CenterHorizontally"), "Column must emit horizontalAlignment. Code:\n$code")
        assertTrue(code.contains("verticalArrangement = Arrangement.SpaceAround"), "Column must emit verticalArrangement. Code:\n$code")
        assertTrue(code.contains("horizontalArrangement = Arrangement.SpaceBetween"), "Row must emit horizontalArrangement. Code:\n$code")
        assertTrue(code.contains("verticalAlignment = Alignment.CenterVertically"), "Row must emit verticalAlignment. Code:\n$code")
        com.facebook.ktfmt.format.Formatter.format(
            com.facebook.ktfmt.format.Formatter.GOOGLE_FORMAT,
            code
        )
    }

    @Test
    fun testCardVariantsGenerateValidComposeCode() {
        val generator = ComposeCodeGenerator()

        val elevatedCard = DesignNode(
            id = NodeId("c-elevated"),
            kind = ComponentKind.Card,
            props = mapOf(
                CardDefinition.Variant.id to PropertyValue.EnumValue(io.github.chandu4221.m3stage.model.CardVariant.Elevated.name),
                CardDefinition.Elevation.id to PropertyValue.DpValue(3f)
            )
        )
        val filledCard = DesignNode(
            id = NodeId("c-filled"),
            kind = ComponentKind.Card,
            props = mapOf(
                CardDefinition.Variant.id to PropertyValue.EnumValue(io.github.chandu4221.m3stage.model.CardVariant.Filled.name)
            )
        )
        val outlinedCard = DesignNode(
            id = NodeId("c-outlined"),
            kind = ComponentKind.Card,
            props = mapOf(
                CardDefinition.Variant.id to PropertyValue.EnumValue(io.github.chandu4221.m3stage.model.CardVariant.Outlined.name),
                CardDefinition.BorderColor.id to PropertyValue.ColorValue.Token(io.github.chandu4221.m3stage.theme.M3ColorToken.Outline),
                CardDefinition.BorderWidth.id to PropertyValue.DpValue(2f)
            )
        )
        val column = DesignNode(
            id = NodeId("root-col"),
            kind = ComponentKind.Column,
            children = listOf(elevatedCard, filledCard, outlinedCard)
        )
        val project = Project(
            id = ProjectId("test-proj-card-variants"),
            name = "Card Variants App",
            basePackage = "com.m3stage.test",
            defaultDevice = DevicePreset.Pixel8,
            screens = listOf(
                Screen(
                    id = ScreenId("screen-card-variants"),
                    name = "CardVariantsScreen",
                    route = "/card-variants",
                    root = column
                )
            )
        )

        val generated = generator.generate(project)
        val code = generated.files.first().content

        assertTrue(code.contains("ElevatedCard("), "Must emit ElevatedCard for Elevated variant. Code:\n$code")
        assertTrue(code.contains("elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.0.dp)"), "Must emit elevatedCardElevation. Code:\n$code")
        assertTrue(code.contains("Card("), "Must emit Card for Filled variant. Code:\n$code")
        assertTrue(code.contains("OutlinedCard("), "Must emit OutlinedCard for Outlined variant. Code:\n$code")
        assertTrue(code.contains("border = BorderStroke(2.0.dp, MaterialTheme.colorScheme.outline)"), "Must emit border for Outlined variant. Code:\n$code")

        com.facebook.ktfmt.format.Formatter.format(
            com.facebook.ktfmt.format.Formatter.GOOGLE_FORMAT,
            code
        )
    }

    @Test
    fun testFabVariantsGenerateValidComposeCode() {
        val generator = ComposeCodeGenerator()

        val primaryFab = DesignNode(
            id = NodeId("fab-p"),
            kind = ComponentKind.FloatingActionButton,
            props = mapOf(
                FloatingActionButtonDefinition.Variant.id to PropertyValue.EnumValue(io.github.chandu4221.m3stage.model.FabVariant.Primary.name)
            )
        )
        val secondaryFab = DesignNode(
            id = NodeId("fab-s"),
            kind = ComponentKind.FloatingActionButton,
            props = mapOf(
                FloatingActionButtonDefinition.Variant.id to PropertyValue.EnumValue(io.github.chandu4221.m3stage.model.FabVariant.Secondary.name)
            )
        )
        val tertiaryFab = DesignNode(
            id = NodeId("fab-t"),
            kind = ComponentKind.FloatingActionButton,
            props = mapOf(
                FloatingActionButtonDefinition.Variant.id to PropertyValue.EnumValue(io.github.chandu4221.m3stage.model.FabVariant.Tertiary.name)
            )
        )
        val column = DesignNode(
            id = NodeId("root-col-fab"),
            kind = ComponentKind.Column,
            children = listOf(primaryFab, secondaryFab, tertiaryFab)
        )
        val project = Project(
            id = ProjectId("test-proj-fab-variants"),
            name = "FAB Variants App",
            basePackage = "com.m3stage.test",
            defaultDevice = DevicePreset.Pixel8,
            screens = listOf(
                Screen(
                    id = ScreenId("screen-fab-variants"),
                    name = "FabVariantsScreen",
                    route = "/fab-variants",
                    root = column
                )
            )
        )

        val generated = generator.generate(project)
        val code = generated.files.first().content

        // Primary: no explicit colors emitted when unset
        assertTrue(code.contains("FloatingActionButton("), "Must emit FloatingActionButton. Code:\n$code")
        // Secondary: emits secondaryContainer and onSecondaryContainer
        assertTrue(code.contains("containerColor = MaterialTheme.colorScheme.secondaryContainer"), "Must emit secondaryContainer for Secondary FAB. Code:\n$code")
        assertTrue(code.contains("contentColor = MaterialTheme.colorScheme.onSecondaryContainer"), "Must emit onSecondaryContainer for Secondary FAB. Code:\n$code")
        // Tertiary: emits tertiaryContainer and onTertiaryContainer
        assertTrue(code.contains("containerColor = MaterialTheme.colorScheme.tertiaryContainer"), "Must emit tertiaryContainer for Tertiary FAB. Code:\n$code")
        assertTrue(code.contains("contentColor = MaterialTheme.colorScheme.onTertiaryContainer"), "Must emit onTertiaryContainer for Tertiary FAB. Code:\n$code")

        com.facebook.ktfmt.format.Formatter.format(
            com.facebook.ktfmt.format.Formatter.GOOGLE_FORMAT,
            code
        )
    }
}


