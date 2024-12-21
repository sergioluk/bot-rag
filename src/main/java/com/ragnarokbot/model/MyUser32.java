package com.ragnarokbot.model;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.POINT;

public interface MyUser32 extends com.sun.jna.Library {
	MyUser32 INSTANCE = Native.load("user32", MyUser32.class);

    boolean ClientToScreen(HWND hWnd, POINT lpPoint);
}
