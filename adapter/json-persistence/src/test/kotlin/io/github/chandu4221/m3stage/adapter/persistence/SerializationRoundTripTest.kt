package io.github.chandu4221.m3stage.adapter.persistence

import io.github.chandu4221.m3stage.adapter.persistence.dto.ProjectDto
import io.github.chandu4221.m3stage.adapter.persistence.dto.toDomain
import io.github.chandu4221.m3stage.adapter.persistence.dto.toDto
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.*
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
        val rootNode = DesignNode(
            id = NodeId("root-scaffold"),
            kind = ComponentKind.Scaffold,
            props = mapOf(
                ComponentCatalog.ScaffoldProps.ContainerColor.id to PropertyValue.ColorValue.Token(M3ColorToken.Background)
            ),
            children = listOf(
                DesignNode(
                    id = NodeId("top-bar"),
                    kind = ComponentKind.TopAppBar,
                    props = mapOf(
                        ComponentCatalog.TopAppBarProps.Title.id to PropertyValue.StringValue("Dashboard"),
                        ComponentCatalog.TopAppBarProps.ContainerColor.id to PropertyValue.ColorValue.Token(M3ColorToken.Surface)
                    ),
                    children = listOf(
                        DesignNode(
                            id = NodeId("action-icon"),
                            kind = ComponentKind.Icon,
                            props = mapOf(
                                ComponentCatalog.IconProps.IconName.id to PropertyValue.StringValue("Favorite")
                            )
                        )
                    )
                ),
                DesignNode(
                    id = NodeId("content-column"),
                    kind = ComponentKind.Column,
                    props = mapOf(
                        ComponentCatalog.ColumnProps.Spacing.id to PropertyValue.DpValue(16f)
                    ),
                    children = listOf(
                        DesignNode(
                            id = NodeId("card-1"),
                            kind = ComponentKind.Card,
                            props = mapOf(
                                ComponentCatalog.CardProps.Elevation.id to PropertyValue.DpValue(4f),
                                ComponentCatalog.CardProps.Shape.id to PropertyValue.ShapeValue.Token(M3ShapeToken.Large)
                            )
                        ),
                        DesignNode(
                            id = NodeId("btn-1"),
                            kind = ComponentKind.Button,
                            props = mapOf(
                                ComponentCatalog.ButtonProps.Enabled.id to PropertyValue.BooleanValue(true)
                            ),
                            children = listOf(
                                DesignNode(
                                    id = NodeId("btn-txt"),
                                    kind = ComponentKind.Text,
                                    props = mapOf(
                                        ComponentCatalog.TextProps.TextContent.id to PropertyValue.StringValue("Submit"),
                                        ComponentCatalog.TextProps.Typography.id to PropertyValue.TypographyValue.Token(M3TypographyToken.LabelLarge)
                                    )
                                )
                            )
                        )
                    )
                )
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