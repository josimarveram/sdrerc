/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 *
 * @author usuario
 */
public class MaxLengthDocumentFilter extends DocumentFilter
{
    private int max;
    public MaxLengthDocumentFilter(int max) {
        this.max = max;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String text, AttributeSet attrs)
            throws BadLocationException {

        if (text == null) return;

        if (fb.getDocument().getLength() + text.length() <= max) {
            super.insertString(fb, offset, text, attrs);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {

        if (text == null) return;

        if (fb.getDocument().getLength() - length + text.length() <= max) {
            super.replace(fb, offset, length, text, attrs);
        }
    }
}
