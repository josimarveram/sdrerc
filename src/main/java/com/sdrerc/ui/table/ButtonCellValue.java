package com.sdrerc.ui.table;

public class ButtonCellValue {

    private final String text;
    private final boolean enabled;

    public ButtonCellValue(String text, boolean enabled) {
        this.text = text;
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return text;
    }
}
