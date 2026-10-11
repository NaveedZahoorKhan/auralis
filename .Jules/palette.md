## 2024-08-22 - Unlabelled Icon Buttons in State changes
**Learning:** When conditionally removing text labels from UI elements to save space (like unselected tabs), it creates "unlabelled button" traps for screen readers if the remaining icon doesn't have a contentDescription.
**Action:** Always map the original text label to the icon's contentDescription in the reduced/collapsed state.
## 2026-08-23 - UI Organization using Material 3
**Learning:** Replacing custom navigation UI with standard Material 3 components like `NavigationBar` and using overflow menus greatly reduces visual clutter and improves standard Android UX compliance.
**Action:** Replaced scrollable `ModeTabs` with `NavigationBar` in Scaffold's `bottomBar` and moved secondary `TopAppBar` actions to an overflow `DropdownMenu`.
## 2026-08-25 - Added Clear Button to Search Field
**Learning:** Added a clear button to a search field to improve micro-UX in Jetpack Compose, including an appropriate `contentDescription` for screen reader accessibility. It's a standard pattern that relies on existing material icons and button components.
**Action:** When adding standard interactive elements (like clear buttons), ensure they conditionally render based on state (e.g. only show when query is not empty) and always include descriptive accessibility text for screen readers.
## 2024-08-27 - Keyboard UX in Mobile Forms
**Learning:** Text input fields in mobile apps often leave the keyboard open after the user finishes typing or hits "enter", causing a frustrating "keyboard trap". This happens when Compose `OutlinedTextField` elements lack explicit `ImeAction` handling.
**Action:** Always include `KeyboardOptions` (with appropriate capitalization and `ImeAction.Done`/`ImeAction.Search`) and `KeyboardActions` (using `LocalFocusManager.current.clearFocus()`) on text fields to naturally dismiss the keyboard when input is complete.
