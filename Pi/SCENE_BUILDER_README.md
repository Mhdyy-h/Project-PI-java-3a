# Scene Builder Guide for Appointment Management System

## Overview
This guide helps you use Scene Builder to view and modify the appointment management interface.

## Files Available for Scene Builder

### 1. Main Appointment Management Interface
- **File**: `src/main/resources/view/rendez_vous_scenebuilder.fxml`
- **Controller**: `org.example.controller.RendezVousController`
- **Size**: 1000x700 pixels
- **Features**:
  - Search and filter section
  - Action buttons (Add, Edit, Delete, Refresh)
  - Data table with appointment listings
  - Status and count display

### 2. Appointment Dialog (Add/Edit)
- **File**: `src/main/resources/view/rendezvous_dialog_scenebuilder.fxml`
- **Controller**: `org.example.controller.RendezVousDialogController`
- **Size**: 600x650 pixels
- **Features**:
  - Complete appointment form
  - Patient and specialist selection
  - Date/time picker
  - Validation information section

## How to Open in Scene Builder

### Method 1: Direct Open
1. Open Scene Builder
2. Go to `File > Open`
3. Navigate to: `src/main/resources/view/`
4. Select either:
   - `rendez_vous_scenebuilder.fxml` (main interface)
   - `rendezvous_dialog_scenebuilder.fxml` (dialog)

### Method 2: Right-Click Open
1. Right-click on the FXML file in your IDE
2. Select "Open in Scene Builder"

## Key Components in Scene Builder

### Main Interface Components
- **Header Section**: Title and status display
- **Search/Filter Section**: Text fields and combo boxes for filtering
- **Action Buttons**: CRUD operations buttons
- **Table View**: Appointment data display
- **Footer Section**: Count and export functionality

### Dialog Components
- **Form Grid**: 9-row form with all appointment fields
- **Validation Section**: Information about validation rules
- **Button Section**: Save/Cancel actions

## Styling Information
- **CSS File**: `src/main/resources/styles.css`
- **Main Style Classes**:
  - `.appointment-table` - Table styling
  - `.primary-button` - Main action buttons
  - `.danger-button` - Delete buttons
  - `.search-filter-section` - Search area
  - `.form-label` - Form field labels

## Controller Integration
Each FXML file is linked to its controller:
- Main interface uses `RendezVousController`
- Dialog uses `RendezVousDialogController`

All `fx:id` attributes match the controller field names for proper injection.

## Customization Tips

### Colors and Themes
- Modify colors in `styles.css`
- Main theme colors: `#4C6FFF` (primary), `#ef4444` (danger)
- Status colors: Green (confirmed), Yellow (pending), Red (cancelled)

### Layout Adjustments
- Use Scene Builder's layout inspector
- Adjust spacing and padding in the Properties panel
- Modify component sizes using the Size section

### Adding New Components
1. Drag components from the Library panel
2. Set appropriate `fx:id` attributes
3. Add corresponding fields in the controller
4. Implement event handlers in the controller class

## Testing the Interface
1. Make changes in Scene Builder
2. Save the FXML file
3. Run your application to see changes
4. The controller will automatically load the updated layout

## Important Notes
- Scene Builder versions: Compatible with JavaFX 17+
- Ensure your project has JavaFX dependencies in `pom.xml`
- CSS styles are linked automatically via the `stylesheets` attribute
- All components have proper IDs for controller injection

## Troubleshooting
- **Missing Controller**: Ensure the controller class exists and is in the correct package
- **CSS Not Loading**: Check the path to the styles.css file
- **Component Errors**: Verify all `fx:id` attributes match controller field names

## Next Steps
1. Open the FXML files in Scene Builder
2. Experiment with layout changes
3. Test different styling options
4. Save and run your application to see results
