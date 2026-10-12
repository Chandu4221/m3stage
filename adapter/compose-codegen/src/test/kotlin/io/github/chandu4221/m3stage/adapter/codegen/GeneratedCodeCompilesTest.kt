package io.github.chandu4221.m3stage.adapter.codegen

import io.github.chandu4221.m3stage.adapter.codegen.resolver.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ModifierCodeResolver.requiredImport
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.ButtonDefinition
import io.github.chandu4221.m3stage.component.definition.TextDefinition
import io.github.chandu4221.m3stage.model.*
import io.github.chandu4221.m3stage.modifier.ModifierScope
import io.github.chandu4221.m3stage.modifier.ModifierSpec
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
}
