## 2024-08-22 - Unlabelled Icon Buttons in State changes
**Learning:** When conditionally removing text labels from UI elements to save space (like unselected tabs), it creates "unlabelled button" traps for screen readers if the remaining icon doesn't have a contentDescription.
**Action:** Always map the original text label to the icon's contentDescription in the reduced/collapsed state.
## 2026-08-23 - UI Organization using Material 3
**Learning:** Replacing custom navigation UI with standard Material 3 components like `NavigationBar` and using overflow menus greatly reduces visual clutter and improves standard Android UX compliance.
**Action:** Replaced scrollable `ModeTabs` with `NavigationBar` in Scaffold's `bottomBar` and moved secondary `TopAppBar` actions to an overflow `DropdownMenu`.
## 2026-08-25 - Added Clear Button to Search Field
**Learning:** Added a clear button to a search field to improve micro-UX in Jetpack Compose, including an appropriate `contentDescription` for screen reader accessibility. It's a standard pattern that relies on existing material icons and button components.
**Action:** When adding standard interactive elements (like clear buttons), ensure they conditionally render based on state (e.g. only show when query is not empty) and always include descriptive accessibility text for screen readers.
## 2024-10-25 - Mobile Keyboard Optimizations for Text Fields
**Learning:** Default text fields on Android keep the keyboard open and don't provide context-specific actions (like 'Search' or 'Done'), leading to poor UX where the keyboard obscures content. Capitalization can also be non-intuitive depending on the input type.
**Action:** Always configure `OutlinedTextField` (and similar text inputs) with appropriate `KeyboardOptions` (for capitalization and `ImeAction`) and `KeyboardActions` (e.g., using `LocalFocusManager.current.clearFocus()`) to optimize mobile text input and naturally dismiss the keyboard.
