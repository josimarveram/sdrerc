package com.sdrerc;

import com.sdrerc.ui.login.FrmLogin;

public class Main {

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");
        System.setProperty("sun.java2d.dpiaware", "true");

        FrmLogin.main(args);
    }
}