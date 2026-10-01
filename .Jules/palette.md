## 2024-08-22 - Unlabelled Icon Buttons in State changes
**Learning:** When conditionally removing text labels from UI elements to save space (like unselected tabs), it creates "unlabelled button" traps for screen readers if the remaining icon doesn't have a contentDescription.
**Action:** Always map the original text label to the icon's contentDescription in the reduced/collapsed state.
## 2026-08-23 - UI Organization using Material 3
**Learning:** Replacing custom navigation UI with standard Material 3 components like `NavigationBar` and using overflow menus greatly reduces visual clutter and improves standard Android UX compliance.
**Action:** Replaced scrollable `ModeTabs` with `NavigationBar` in Scaffold's `bottomBar` and moved secondary `TopAppBar` actions to an overflow `DropdownMenu`.
## 2026-08-25 - Added Clear Button to Search Field
**Learning:** Added a clear button to a search field to improve micro-UX in Jetpack Compose, including an appropriate `contentDescription` for screen reader accessibility. It's a standard pattern that relies on existing material icons and button components.
**Action:** When adding standard interactive elements (like clear buttons), ensure they conditionally render based on state (e.g. only show when query is not empty) and always include descriptive accessibility text for screen readers.
## 2026-08-27 - Mobile Keyboard Optimizations for OutlinedTextField
**Learning:** The default Android `OutlinedTextField` does not optimize the software keyboard for its context. For example, search fields will show a generic "Enter" or return key, and failing to handle `KeyboardActions` means the keyboard won't dismiss intuitively.
**Action:** When creating text inputs on mobile, always configure `KeyboardOptions` (e.g., `imeAction = ImeAction.Search`) and `KeyboardActions` (e.g., dismissing the keyboard with `LocalFocusManager.current.clearFocus()`) to provide appropriate contextual keys and actions.
