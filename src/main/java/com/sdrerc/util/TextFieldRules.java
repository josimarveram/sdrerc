/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.util;

import javax.swing.JTextField;
import javax.swing.text.*;

/**
 *
 * @author usuario
 */
public class TextFieldRules 
{
    private JTextField textField;
    private RuleFilter filter;

    private TextFieldRules(JTextField textField) {
        this.textField = textField;
        this.filter = new RuleFilter();
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(filter);
    }

    public static TextFieldRules apply(JTextField textField) {
        return new TextFieldRules(textField);
    }

    // -------------------------------
    // Reglas encadenables
    // -------------------------------

    public TextFieldRules onlyNumbers() {
        filter.onlyNumbers = true;
        return this;
    }

    public TextFieldRules onlyLetters() {
        filter.onlyLetters = true;
        return this;
    }

    public TextFieldRules max(int length) {
        filter.maxLength = length;
        return this;
    }

    public TextFieldRules min(int length) {
        filter.minLength = length;
        return this;
    }

    // ===============================
    //  FILTRO INTERNO
    // ===============================

    private static class RuleFilter extends DocumentFilter {

        boolean onlyNumbers = false;
        boolean onlyLetters = false;
        Integer maxLength = null;
        Integer minLength = null;

        @Override
        public void insertString(FilterBypass fb, int offset, String text, AttributeSet attrs)
                throws BadLocationException {

            if (validate(fb, text, 0))
                super.insertString(fb, offset, text, attrs);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {

            if (validate(fb, text, length))
                super.replace(fb, offset, length, text, attrs);
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length)
                throws BadLocationException {

            if (minLength == null) {
                super.remove(fb, offset, length);
            } else {
                int newLength = fb.getDocument().getLength() - length;
                if (newLength >= minLength) {
                    super.remove(fb, offset, length);
                }
            }
        }

        private boolean validate(FilterBypass fb, String text, int lengthToReplace) {
            if (text == null) return false;

            String newText = getNewText(fb, text, lengthToReplace);

            // Solo números
            if (onlyNumbers && !newText.matches("\\d*")) return false;

            // Solo letras
            if (onlyLetters && !newText.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]*")) return false;

            // MaxLength
            if (maxLength != null && newText.length() > maxLength) return false;

            // MinLength
            if (minLength != null && newText.length() < minLength) return false;

            return true;
        }

        private String getNewText(FilterBypass fb, String text, int lengthToReplace) {
            try {
                String current = fb.getDocument().getText(0, fb.getDocument().getLength());
                StringBuilder sb = new StringBuilder(current);

                int start = fb.getDocument().getLength() - lengthToReplace;
                if (start < 0) start = 0;

                sb.replace(start, fb.getDocument().getLength(), text);

                return sb.toString();
            } catch (Exception e) {
                return text;
            }
        }
    }
}
