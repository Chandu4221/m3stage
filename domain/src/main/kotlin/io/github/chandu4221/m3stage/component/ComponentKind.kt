package io.github.chandu4221.m3stage.component

enum class ComponentKind(
    val id: String,
    val displayName: String,
    val category: ComponentCategory,
    val module: ComposePackage,
    val isContainer: Boolean
) {
    // --- COMMUNICATION ---
    Text(
        id = "Text",
        displayName = "Text",
        category = ComponentCategory.Communication,
        module = ComposePackage.Material3,
        isContainer = false
    ),
    Icon(
        id = "Icon",
        displayName = "Icon",
        category = ComponentCategory.Communication,
        module = ComposePackage.Material3,
        isContainer = false
    ),
    Image(
        id = "Image",
        displayName = "Image",
        category = ComponentCategory.Communication,
        module = ComposePackage.Foundation,
        isContainer = false
    ),

    Badge(
        id = "Badge",
        displayName = "Badge",
        category = ComponentCategory.Communication,
        module = ComposePackage.Material3,
        isContainer = true
    ),

    // --- ACTIONS ---
    Button(
        id = "Button",
        displayName = "Button",
        category = ComponentCategory.Actions,
        module = ComposePackage.Material3,
        isContainer = true
    ),

    IconButton(
        id = "IconButton",
        displayName = "IconButton",
        category = ComponentCategory.Actions,
        module = ComposePackage.Material3,
        isContainer = true
    ),
    FloatingActionButton(
        id = "FloatingActionButton",
        displayName = "FAB",
        category = ComponentCategory.Actions,
        module = ComposePackage.Material3,
        isContainer = true
    ),
    OutlinedButton(
        id = "OutlinedButton",
        displayName = "OutlinedButton",
        category = ComponentCategory.Actions,
        module = ComposePackage.Material3,
        isContainer = true
    ),
    TextButton(
        id = "TextButton",
        displayName = "TextButton",
        category = ComponentCategory.Actions,
        module = ComposePackage.Material3,
        isContainer = true
    ),

    // --- TEXT INPUTS ---
    TextField(
        id = "TextField",
        displayName = "TextField",
        category = ComponentCategory.TextInputs,
        module = ComposePackage.Material3,
        isContainer = false
    ),
    OutlinedTextField(
        id = "OutlinedTextField",
        displayName = "OutlinedTextField",
        category = ComponentCategory.TextInputs,
        module = ComposePackage.Material3,
        isContainer = true
    ),

    // --- LAYOUTS ---
    Column(
        id = "Column",
        displayName = "Column",
        category = ComponentCategory.Layouts,
        module = ComposePackage.FoundationLayout,
        isContainer = true
    ),
    Row(
        id = "Row",
        displayName = "Row",
        category = ComponentCategory.Layouts,
        module = ComposePackage.FoundationLayout,
        isContainer = true
    ),
    Box(
        id = "Box",
        displayName = "Box",
        category = ComponentCategory.Layouts,
        module = ComposePackage.FoundationLayout,
        isContainer = true
    ),

    // --- CONTAINMENT ---
    Scaffold(
        id = "Scaffold",
        displayName = "Scaffold",
        category = ComponentCategory.Containment,
        module = ComposePackage.Material3,
        isContainer = true
    ),

    Card(
        id = "Card",
        displayName = "Card",
        category = ComponentCategory.Containment,
        module = ComposePackage.Material3,
        isContainer = true
    ),

    // --- NAVIGATION ---
    TopAppBar(
        id = "TopAppBar",
        displayName = "TopAppBar",
        category = ComponentCategory.Navigation,
        module = ComposePackage.Material3,
        isContainer = true
    ),

    NavigationBar(
        id = "NavigationBar",
        displayName = "NavigationBar",
        category = ComponentCategory.Navigation,
        module = ComposePackage.Material3,
        isContainer = true
    ),

    Spacer(
        id = "Spacer",
        displayName = "Spacer",
        category = ComponentCategory.Layouts,
        module = ComposePackage.FoundationLayout,
        isContainer = false
    ),
    HorizontalDivider(
        id = "HorizontalDivider",
        displayName = "Divider",
        category = ComponentCategory.Containment,
        module = ComposePackage.Material3,
        isContainer = false
    ),

    // --- SELECTION ---
    Switch(
        id = "Switch",
        displayName = "Switch",
        category = ComponentCategory.Selection,
        module = ComposePackage.Material3,
        isContainer = false
    ),
    Checkbox(
        id = "Checkbox",
        displayName = "Checkbox",
        category = ComponentCategory.Selection,
        module = ComposePackage.Material3,
        isContainer = false
    ),
    RadioButton(
        id = "RadioButton",
        displayName = "RadioButton",
        category = ComponentCategory.Selection,
        module = ComposePackage.Material3,
        isContainer = false
    ),
    Slider(
        id = "Slider",
        displayName = "Slider",
        category = ComponentCategory.Selection,
        module = ComposePackage.Material3,
        isContainer = false
    ),
}