---
name: labmanufacture-style
description: Maintain and review the LabManufacture Thymeleaf UI style. Use when changing templates, tables, search fields, operation buttons, modal dialogs, navigation, responsive behavior, or visual consistency in this repository.
---

# LabManufacture UI Style

Apply the existing visual language consistently across board and front-assembly pages. Preserve the user's requested colors and page-specific operation semantics while removing visual drift.

## Source of truth

Use `src/main/resources/static/styles.css` for shared component geometry and behavior. Before adding CSS to a template, search this file and reuse or extend an existing class.

Do not define table, search, empty-state, action-layout, focus, or modal geometry inside template `<style>` blocks. Keep template-local CSS only for behavior or semantic colors unique to that page. When the same rule appears on more than one page, move it to the shared stylesheet.

Do not restore `front-assembly.css`; assembly pages use the same shared component styles as board pages.

## Visual rules

- Use an 8 px radius for cards, forms, inputs, buttons, tables, alerts, and modal dialogs.
- Keep circles circular: avatars and floating round navigation buttons may use `50%`.
- Use solid colors by default. Preserve the established `#0b499b` to `#7fa5c9` gradient only for `.btn-complete` (the “Сделано” action) unless the user requests another treatment.
- Preserve the established semantic colors: blue for primary actions, green for successful operations, red for failed or destructive operations, and gray for neutral actions.
- Keep existing page-specific colors when a task does not ask to change the palette.
- Use the shared `:focus-visible` treatment for keyboard focus. Do not remove outlines without an equally visible replacement.

## Tables and search

Use this structure where applicable:

- `.search-container` containing a positioned `.search-input` and `.search-icon`.
- `.device-table` containing `.table-header`, `.table-responsive`, and a Bootstrap `.table`.
- `.action-buttons` for groups of row actions.
- `.empty-state` when there is no data.

Keep search and table containers at the same available width. Center action buttons in their column and keep each action group on one line. Let the responsive table scroll horizontally when the row needs more width. Keep sortable labels and their icons on one line.

Every wide table must be inside `.table-responsive`. Retain the shared minimum table width so narrow screens scroll horizontally instead of compressing content into unreadable columns. Add a page-specific column width only when the data requires it; do not reintroduce a complete local table layout.

## Buttons and modal dialogs

Use the shared button height, padding, radius, and action spacing. A row with several actions uses `.action-buttons` rather than margins on individual buttons.

Use Bootstrap modal structure with `.modal-content`, `.modal-header`, `.modal-body`, and `.modal-footer`. The shared stylesheet owns their sizes, padding, radius, title typography, and footer alignment. Template-local modal rules may set only the semantic header or confirmation-button color.

Place cancel and confirmation actions together in the centered footer. Use consistent labels and icons with similar operation pages.

## User menu

Keep the user dropdown compact: 270 px desktop width, 1 rem container padding, compact item padding, and a 38 px avatar. Preserve readable text and touch targets. On narrow screens, keep the dropdown within the viewport.

## Workflow

1. Inspect `styles.css`, the target template, and one equivalent board or assembly page.
2. Reuse shared classes and remove any duplicate local component declarations in the files being changed.
3. Preserve Thymeleaf expressions, security attributes, form actions, modal IDs, and JavaScript hooks while editing markup.
4. Check all changed style blocks for balanced braces and run `git diff --check`.
5. Search the changed scope for local table/search selectors and `linear-gradient`; the result should be empty unless the user explicitly requested an exception.
6. Run relevant template/controller tests when available. If integration tests fail for database access or pre-existing authorization expectations, report that separately from visual verification.

For a review-only request, report inconsistencies and suggested fixes without modifying files.
