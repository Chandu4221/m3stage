package io.github.chandu4221.m3stage.component

enum class ComponentKind(
    val id: String,
    val displayName: String,
    val category: ComponentCategory,
    val module: ComposeModule,
    val isContainer: Boolean
) {
    // --- DISPLAY ---
    Text(
        id = "Text",
        displayName = "Text",
        category = ComponentCategory.Display,
        module = ComposeModule.Material3,
        isContainer = false
    ),
    Icon(
        id = "Icon",
        displayName = "Icon",
        category = ComponentCategory.Display,
        module = ComposeModule.Material3,
        isContainer = false
    ),
    Image(
        id = "Image",
        displayName = "Image",
        category = ComponentCategory.Display,
        module = ComposeModule.Foundation,
        isContainer = false
    ),

    // --- INPUTS ---
    Button(
        id = "Button",
        displayName = "Button",
        category = ComponentCategory.Inputs,
        module = ComposeModule.Material3,
        isContainer = true
    ),
    TextField(
        id = "TextField",
        displayName = "TextField",
        category = ComponentCategory.Inputs,
        module = ComposeModule.Material3,
        isContainer = false
    ),

    // --- LAYOUTS ---
    Column(
        id = "Column",
        displayName = "Column",
        category = ComponentCategory.Layouts,
        module = ComposeModule.FoundationLayout,
        isContainer = true
    ),
    Row(
        id = "Row",
        displayName = "Row",
        category = ComponentCategory.Layouts,
        module = ComposeModule.FoundationLayout,
        isContainer = true
    ),
    Box(
        id = "Box",
        displayName = "Box",
        category = ComponentCategory.Layouts,
        module = ComposeModule.FoundationLayout,
        isContainer = true
    ),
    Scaffold(
        id = "Scaffold",
        displayName = "Scaffold",
        category = ComponentCategory.Layouts,
        module = ComposeModule.Material3,
        isContainer = true
    ),

    // --- SURFACES ---
    Card(
        id = "Card",
        displayName = "Card",
        category = ComponentCategory.Surfaces,
        module = ComposeModule.Material3,
        isContainer = true
    ),

    // --- NAVIGATION ---
    TopAppBar(
        id = "TopAppBar",
        displayName = "TopAppBar",
        category = ComponentCategory.Navigation,
        module = ComposeModule.Material3,
        isContainer = true
    )
}