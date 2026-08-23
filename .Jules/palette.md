## 2024-08-22 - Unlabelled Icon Buttons in State changes
**Learning:** When conditionally removing text labels from UI elements to save space (like unselected tabs), it creates "unlabelled button" traps for screen readers if the remaining icon doesn't have a contentDescription.
**Action:** Always map the original text label to the icon's contentDescription in the reduced/collapsed state.

## 2024-05-18 - Input fields missing quick-clear
**Learning:** For mobile experiences, forcing users to repeatedly hit backspace to clear a search query is poor UX. Adding a conditional trailing "Clear" button significantly improves interaction speed and smoothness.
**Action:** Always check `TextField` and `OutlinedTextField` implementations for search or filter inputs and verify they have a trailing icon button to quickly clear the input state.
