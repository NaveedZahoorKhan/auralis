## 2024-08-22 - Unlabelled Icon Buttons in State changes
**Learning:** When conditionally removing text labels from UI elements to save space (like unselected tabs), it creates "unlabelled button" traps for screen readers if the remaining icon doesn't have a contentDescription.
**Action:** Always map the original text label to the icon's contentDescription in the reduced/collapsed state.
## 2026-08-23 - UI Organization using Material 3
**Learning:** Replacing custom navigation UI with standard Material 3 components like `NavigationBar` and using overflow menus greatly reduces visual clutter and improves standard Android UX compliance.
**Action:** Replaced scrollable `ModeTabs` with `NavigationBar` in Scaffold's `bottomBar` and moved secondary `TopAppBar` actions to an overflow `DropdownMenu`.
## 2026-08-25 - Added Clear Button to Search Field
**Learning:** Added a clear button to a search field to improve micro-UX in Jetpack Compose, including an appropriate `contentDescription` for screen reader accessibility. It's a standard pattern that relies on existing material icons and button components.
**Action:** When adding standard interactive elements (like clear buttons), ensure they conditionally render based on state (e.g. only show when query is not empty) and always include descriptive accessibility text for screen readers.

## 2026-09-30 - Accessible Content Descriptions for Interactive Icons
**Learning:** Decorative icons can safely have a null content description, but icons inside buttons or interactive elements must have a meaningful content description for screen readers to properly interpret their purpose.
**Action:** Always provide descriptive `contentDescription` strings for icons used as actionable elements (e.g., inside `IconButton` or `Button`), rather than defaulting to `null`.

## 2026-09-30 - Keyboard Options for Text Fields
**Learning:** When using Jetpack Compose `OutlinedTextField` for inputs like search or notes, utilizing `KeyboardOptions` and `KeyboardActions` greatly improves the mobile experience (e.g., auto-capitalization, dismissing keyboard on search action).
**Action:** Always configure `KeyboardOptions` with the correct `ImeAction` or `KeyboardCapitalization` and handle the action (e.g., hiding the keyboard) via `KeyboardActions` when adding or modifying text fields.
