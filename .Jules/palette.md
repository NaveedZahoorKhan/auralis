## 2024-08-22 - Unlabelled Icon Buttons in State changes
**Learning:** When conditionally removing text labels from UI elements to save space (like unselected tabs), it creates "unlabelled button" traps for screen readers if the remaining icon doesn't have a contentDescription.
**Action:** Always map the original text label to the icon's contentDescription in the reduced/collapsed state.
## 2026-08-23 - UI Organization using Material 3
**Learning:** Replacing custom navigation UI with standard Material 3 components like `NavigationBar` and using overflow menus greatly reduces visual clutter and improves standard Android UX compliance.
**Action:** Replaced scrollable `ModeTabs` with `NavigationBar` in Scaffold's `bottomBar` and moved secondary `TopAppBar` actions to an overflow `DropdownMenu`.
## 2026-08-25 - Added Clear Button to Search Field
**Learning:** Added a clear button to a search field to improve micro-UX in Jetpack Compose, including an appropriate `contentDescription` for screen reader accessibility. It's a standard pattern that relies on existing material icons and button components.
**Action:** When adding standard interactive elements (like clear buttons), ensure they conditionally render based on state (e.g. only show when query is not empty) and always include descriptive accessibility text for screen readers.
## 2026-08-27 - Mobile Keyboard UX on TextFields
**Learning:** Forgetting to configure `KeyboardOptions` and `KeyboardActions` on Jetpack Compose `TextField`/`OutlinedTextField` results in a poor mobile experience (e.g. lack of sentence capitalization, wrong return key, keyboard staying open after submitting).
**Action:** Always configure `KeyboardOptions` (for capitalization and `ImeAction`) and `KeyboardActions` (to dismiss the keyboard via `LocalFocusManager.current.clearFocus()`) when implementing text inputs to ensure a native, frictionless mobile UX.
