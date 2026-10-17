## 2024-08-22 - Unlabelled Icon Buttons in State changes
**Learning:** When conditionally removing text labels from UI elements to save space (like unselected tabs), it creates "unlabelled button" traps for screen readers if the remaining icon doesn't have a contentDescription.
**Action:** Always map the original text label to the icon's contentDescription in the reduced/collapsed state.
## 2026-08-23 - UI Organization using Material 3
**Learning:** Replacing custom navigation UI with standard Material 3 components like `NavigationBar` and using overflow menus greatly reduces visual clutter and improves standard Android UX compliance.
**Action:** Replaced scrollable `ModeTabs` with `NavigationBar` in Scaffold's `bottomBar` and moved secondary `TopAppBar` actions to an overflow `DropdownMenu`.
## 2026-08-25 - Added Clear Button to Search Field
**Learning:** Added a clear button to a search field to improve micro-UX in Jetpack Compose, including an appropriate `contentDescription` for screen reader accessibility. It's a standard pattern that relies on existing material icons and button components.
**Action:** When adding standard interactive elements (like clear buttons), ensure they conditionally render based on state (e.g. only show when query is not empty) and always include descriptive accessibility text for screen readers.
## 2026-08-26 - Optimized Text Input on Mobile
**Learning:** Out-of-the-box text fields often use the default enter key, which simply adds a new line or keeps focus, creating a frustrating experience. Properly defining standard mobile input behaviors prevents keyboard traps and confusing interfaces.
**Action:** When using Jetpack Compose text input fields (`OutlinedTextField`), always configure `KeyboardOptions` (for `ImeAction.Search`, `ImeAction.Next`, `ImeAction.Done`) and `KeyboardActions` (to call `focusManager.clearFocus()`) to ensure optimal mobile text input behavior and proper keyboard dismissal.