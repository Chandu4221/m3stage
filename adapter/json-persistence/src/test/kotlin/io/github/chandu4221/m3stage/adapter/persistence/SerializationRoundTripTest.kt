package io.github.chandu4221.m3stage.adapter.persistence

import io.github.chandu4221.m3stage.adapter.persistence.dto.ProjectDto
import io.github.chandu4221.m3stage.adapter.persistence.mapper.toDomain
import io.github.chandu4221.m3stage.adapter.persistence.mapper.toDto
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.ButtonDefinition
import io.github.chandu4221.m3stage.component.definition.CardDefinition
import io.github.chandu4221.m3stage.component.definition.ColumnDefinition
import io.github.chandu4221.m3stage.component.definition.IconDefinition
import io.github.chandu4221.m3stage.component.definition.ScaffoldDefinition
import io.github.chandu4221.m3stage.component.definition.TextDefinition
import io.github.chandu4221.m3stage.component.definition.TopAppBarDefinition
import io.github.chandu4221.m3stage.model.*
import io.github.chandu4221.m3stage.modifier.ModifierSpec
import io.github.chandu4221.m3stage.property.PropertyValue
import io.github.chandu4221.m3stage.theme.M3ColorToken
import io.github.chandu4221.m3stage.theme.M3ShapeToken
import io.github.chandu4221.m3stage.theme.M3TypographyToken
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class SerializationRoundTripTest {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    @Test
    fun testProjectRoundTripSerializationInvariance() {
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
                ModifierSpec.FillMaxWidth(1f),
                ModifierSpec.ZIndex(2f)
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
            children = listOf(btnText),
            slots = mapOf(
                ButtonDefinition.LabelSlot.id to listOf(btnText)
            )
        )

        val card = DesignNode(
            id = NodeId("card-1"),
            kind = ComponentKind.Card,
            props = mapOf(
                CardDefinition.Elevation.id to PropertyValue.DpValue(4f),
                CardDefinition.Shape.id to PropertyValue.ShapeValue.Token(M3ShapeToken.Large)
            ),
            modifiers = listOf(
                ModifierSpec.AspectRatio(1.5f),
                ModifierSpec.Scale(1.1f),
                ModifierSpec.Alpha(0.9f)
            )
        )

        val contentColumn = DesignNode(
            id = NodeId("content-column"),
            kind = ComponentKind.Column,
            props = mapOf(
                ColumnDefinition.Spacing.id to PropertyValue.DpValue(16f)
            ),
            modifiers = listOf(
                ModifierSpec.Padding.all(16f),
                ModifierSpec.VerticalScroll(enabled = true),
                ModifierSpec.HorizontalScroll(enabled = false)
            ),
            children = listOf(card, button)
        )

        val rootNode = DesignNode(
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

        val originalProject = Project(
            id = ProjectId("test-proj"),
            name = "Test App",
            basePackage = "com.m3stage.test",
            defaultDevice = DevicePreset.Pixel8,
            screens = listOf(
                Screen(
                    id = ScreenId("screen-1"),
                    name = "Home",
                    route = "/",
                    device = DevicePreset.Pixel8,
                    root = rootNode
                )
            ),
            seedColor = 0xFF6750A4L,
            isDarkMode = true
        )

        // 1. Domain -> DTO
        val dto = originalProject.toDto()

        // 2. DTO -> JSON String
        val jsonString = json.encodeToString(dto)

        // 3. JSON String -> DTO
        val decodedDto = json.decodeFromString<ProjectDto>(jsonString)

        // 4. DTO -> Domain
        val roundTrippedProject = decodedDto.toDomain()

        // 5. Invariant Assertion
        assertEquals(originalProject, roundTrippedProject, "Round-tripped project must match original project exactly")
    }

    @Test
    fun testEveryModifierTypeRoundTripsLosslessly() {
        val allModifiers = listOf(
            // 1. Padding
            ModifierSpec.Padding(start = 1f, top = 2f, end = 3f, bottom = 4f),
            // 2. Size
            ModifierSpec.FillMaxWidth(fraction = 0.8f),
            ModifierSpec.FillMaxHeight(fraction = 0.9f),
            ModifierSpec.FillMaxSize(fraction = 0.95f),
            ModifierSpec.Size(width = 120f, height = 48f),
            ModifierSpec.Width(width = 200f),
            ModifierSpec.Height(height = 56f),
            ModifierSpec.WrapContentSize(unbounded = true),
            ModifierSpec.Weight(weight = 2.5f, fill = false),
            ModifierSpec.AspectRatio(ratio = 1.77f),
            // 3. Position & Order
            ModifierSpec.Offset(x = 8f, y = 16f),
            ModifierSpec.ZIndex(zIndex = 5f),
            // 4. Drawing & Border
            ModifierSpec.Background(
                color = PropertyValue.ColorValue.Custom(0xFF123456L),
                shape = PropertyValue.ShapeValue.Token(M3ShapeToken.Medium)
            ),
            ModifierSpec.Border(
                width = 2f,
                color = PropertyValue.ColorValue.Token(M3ColorToken.Outline),
                shape = PropertyValue.ShapeValue.CornerDp(topStart = 4f, topEnd = 8f, bottomEnd = 12f, bottomStart = 16f)
            ),
            ModifierSpec.Shadow(
                elevation = 8f,
                shape = PropertyValue.ShapeValue.UniformDp(10f),
                clip = true
            ),
            // 5. Graphics Layer
            ModifierSpec.Clip(shape = PropertyValue.ShapeValue.Token(M3ShapeToken.Large)),
            ModifierSpec.Alpha(alpha = 0.75f),
            // 6. Transformations
            ModifierSpec.Rotate(degrees = 180f),
            ModifierSpec.Scale(scale = 1.5f),
            // 7. Scroll
            ModifierSpec.VerticalScroll(enabled = true),
            ModifierSpec.HorizontalScroll(enabled = false)
        )

        val invisibleChild = DesignNode(
            id = NodeId("invisible-child"),
            kind = ComponentKind.Text,
            props = mapOf(TextDefinition.TextContent.id to PropertyValue.StringValue("Hidden")),
            isVisible = false
        )

        val occupiedChild = DesignNode(
            id = NodeId("occupied-child"),
            kind = ComponentKind.Icon,
            props = mapOf(IconDefinition.IconName.id to PropertyValue.StringValue("Settings")),
            isVisible = true
        )

        val nodeWithAllModifiers = DesignNode(
            id = NodeId("full-spec-node"),
            kind = ComponentKind.Box,
            modifiers = allModifiers,
            children = listOf(invisibleChild),
            slots = mapOf(
                io.github.chandu4221.m3stage.component.slot.SlotId("emptySlot") to emptyList(),
                io.github.chandu4221.m3stage.component.slot.SlotId("occupiedSlot") to listOf(occupiedChild)
            ),
            isVisible = true
        )

        val dto = nodeWithAllModifiers.toDto()
        val jsonString = json.encodeToString(dto)
        val decodedDto = json.decodeFromString<io.github.chandu4221.m3stage.adapter.persistence.dto.DesignNodeDto>(jsonString)
        val decodedNode = decodedDto.toDomain()

        assertEquals(nodeWithAllModifiers, decodedNode, "DesignNode with all 21 modifiers and slots must round-trip losslessly")
    }

    @Test
    fun testScreenDeviceAndOrientationRoundTripLosslessly() {
        val rootNode = DesignNode(
            id = NodeId("root-scaffold"),
            kind = ComponentKind.Scaffold
        )
        val originalProject = Project(
            id = ProjectId("test-proj-orientation"),
            name = "Orientation Test App",
            basePackage = "com.m3stage.test",
            defaultDevice = DevicePreset.Pixel8,
            screens = listOf(
                Screen(
                    id = ScreenId("screen-1"),
                    name = "Home",
                    route = "/",
                    device = DevicePreset.GalaxyS24,
                    orientation = DeviceOrientation.Landscape,
                    root = rootNode
                )
            )
        )

        val dto = originalProject.toDto()
        assertEquals(2, dto.schemaVersion)
        assertEquals("Landscape", dto.screens.first().orientation)

        val jsonString = json.encodeToString(dto)
        val decodedDto = json.decodeFromString<ProjectDto>(jsonString)
        val restoredProject = decodedDto.toDomain()

        assertEquals(originalProject, restoredProject)
        assertEquals(DeviceOrientation.Landscape, restoredProject.screens.first().orientation)
        assertEquals(DevicePreset.GalaxyS24, restoredProject.screens.first().device)
    }

    @Test
    fun testSchemaV1BackwardsCompatibilityLoadsWithDefaultOrientation() {
        val v1Json = """
            {
                "id": "proj-v1",
                "name": "Legacy V1",
                "basePackage": "com.example",
                "schemaVersion": 1,
                "defaultDevice": "pixel_8",
                "screens": [
                    {
                        "id": "s-1",
                        "name": "Main",
                        "route": "/main",
                        "device": "pixel_8",
                        "root": {
                            "id": "root",
                            "type": "Scaffold",
                            "props": {},
                            "modifiers": [],
                            "children": [],
                            "slots": {},
                            "isVisible": true
                        }
                    }
                ]
            }
        """.trimIndent()

        val decodedDto = json.decodeFromString<ProjectDto>(v1Json)
        val domainProject = decodedDto.toDomain()

        assertEquals(1, domainProject.screens.size)
        assertEquals(DeviceOrientation.Portrait, domainProject.screens.first().orientation)
    }

    @Test
    fun testNavigationBarAndItemsRoundTripLosslessly() {
        val idGen = object : io.github.chandu4221.m3stage.port.IdGenerator {
            private var c = 1
            override fun nextNodeId(): NodeId = NodeId("node_${c++}")
            override fun nextScreenId(): ScreenId = ScreenId("s_${c++}")
            override fun nextProjectId(): ProjectId = ProjectId("p_${c++}")
        }
        val navBar = io.github.chandu4221.m3stage.component.ComponentCatalog[ComponentKind.NavigationBar].createDefaultSubtree(idGen)
        val dto = navBar.toDto()
        val jsonString = json.encodeToString(dto)
        val decodedDto = json.decodeFromString<io.github.chandu4221.m3stage.adapter.persistence.dto.DesignNodeDto>(jsonString)
        val decoded = decodedDto.toDomain()

        assertEquals(navBar, decoded, "NavigationBar with prefilled NavigationBarItems must round trip losslessly")
    }
}

