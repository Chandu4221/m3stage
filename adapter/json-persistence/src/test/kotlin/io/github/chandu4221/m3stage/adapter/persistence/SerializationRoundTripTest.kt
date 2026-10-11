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
}
