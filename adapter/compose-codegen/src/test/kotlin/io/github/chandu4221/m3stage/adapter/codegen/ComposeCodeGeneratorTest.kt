package io.github.chandu4221.m3stage.adapter.codegen

import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.*
import io.github.chandu4221.m3stage.model.*
import io.github.chandu4221.m3stage.modifier.ModifierSpec
import io.github.chandu4221.m3stage.property.PropertyValue
import io.github.chandu4221.m3stage.theme.M3ColorToken
import io.github.chandu4221.m3stage.theme.M3TypographyToken
import kotlin.test.Test
import kotlin.test.assertTrue

class ComposeCodeGeneratorTest {

    @Test
    fun testGenerateScreenWithSlotsAndModifiers() {
        val actionIcon = DesignNode(
            id = NodeId("action-icon"),
            kind = ComponentKind.Icon,
            props = mapOf(
                IconDefinition.IconName.id to PropertyValue.StringValue("Favorite")
            ),
            modifiers = listOf(
                ModifierSpec.Size(24f, 24f),
                ModifierSpec.Rotate(45f)
            )
        )

        val topBar = DesignNode(
            id = NodeId("top-bar"),
            kind = ComponentKind.TopAppBar,
            props = mapOf(
                TopAppBarDefinition.Title.id to PropertyValue.StringValue("Dashboard"),
                TopAppBarDefinition.ContainerColor.id to PropertyValue.ColorValue.Token(M3ColorToken.Surface)
            ),
            modifiers = listOf(
                ModifierSpec.FillMaxWidth(1f)
            ),
            slots = mapOf(
                TopAppBarDefinition.ActionsSlot.id to listOf(actionIcon)
            )
        )

        val btnText = DesignNode(
            id = NodeId("btn-txt"),
            kind = ComponentKind.Text,
            props = mapOf(
                TextDefinition.TextContent.id to PropertyValue.StringValue("Submit"),
                TextDefinition.Typography.id to PropertyValue.TypographyValue.Token(M3TypographyToken.LabelLarge)
            )
        )

        val button = DesignNode(
            id = NodeId("btn-1"),
            kind = ComponentKind.Button,
            props = mapOf(
                ButtonDefinition.Enabled.id to PropertyValue.BooleanValue(true)
            ),
            modifiers = listOf(
                ModifierSpec.Weight(1f, fill = true)
            ),
            slots = mapOf(
                ButtonDefinition.LabelSlot.id to listOf(btnText)
            )
        )

        val boxChild = DesignNode(
            id = NodeId("box-child"),
            kind = ComponentKind.Text,
            props = mapOf(
                TextDefinition.TextContent.id to PropertyValue.StringValue("Overlay")
            ),
            modifiers = listOf(
                ModifierSpec.ZIndex(2f)
            )
        )

        val overlayBox = DesignNode(
            id = NodeId("overlay-box"),
            kind = ComponentKind.Box,
            children = listOf(boxChild)
        )

        val contentColumn = DesignNode(
            id = NodeId("content-column"),
            kind = ComponentKind.Column,
            props = mapOf(
                ColumnDefinition.Spacing.id to PropertyValue.DpValue(16f)
            ),
            modifiers = listOf(
                ModifierSpec.Padding.all(16f),
                ModifierSpec.VerticalScroll(enabled = true)
            ),
            children = listOf(button, overlayBox)
        )

        val rootScaffold = DesignNode(
            id = NodeId("root-scaffold"),
            kind = ComponentKind.Scaffold,
            props = mapOf(
                ScaffoldDefinition.ContainerColor.id to PropertyValue.ColorValue.Token(M3ColorToken.Background)
            ),
            modifiers = listOf(
                ModifierSpec.FillMaxSize(1f)
            ),
            slots = mapOf(
                ScaffoldDefinition.TopBarSlot.id to listOf(topBar),
                ScaffoldDefinition.ContentSlot.id to listOf(contentColumn)
            )
        )

        val project = Project(
            id = ProjectId("test-proj"),
            name = "Test App",
            basePackage = "com.m3stage.test",
            defaultDevice = DevicePreset.Pixel8,
            screens = listOf(
                Screen(
                    id = ScreenId("screen-1"),
                    name = "HomeScreen",
                    route = "/home",
                    root = rootScaffold
                )
            )
        )

        val generator = ComposeCodeGenerator()
        val generatedProject = generator.generate(project)

        val homeFile = generatedProject.files.firstOrNull { it.fileName == "HomeScreen.kt" }
        requireNotNull(homeFile) { "HomeScreen.kt must be generated" }

        val code = homeFile.content
        println("GENERATED CODE:\n$code")

        assertTrue(code.contains("fun HomeScreen()"), "Must define Composable function HomeScreen")
        assertTrue(code.contains("Scaffold("), "Must generate Scaffold")
        assertTrue(code.contains("topBar = {"), "Must generate Scaffold topBar slot")
        assertTrue(code.contains("TopAppBar("), "Must generate TopAppBar")
        assertTrue(code.contains("actions = {"), "Must generate TopAppBar actions slot")
        assertTrue(code.contains("Button("), "Must generate Button")
        assertTrue(code.contains(".verticalScroll(rememberScrollState())"), "Must generate verticalScroll modifier")
        assertTrue(code.contains(".weight(1.0f)"), "Must generate weight modifier")
        assertTrue(code.contains(".rotate(45.0f)"), "Must generate rotate modifier")
        assertTrue(code.contains(".zIndex(2.0f)"), "Must generate zIndex modifier")
    }
}
