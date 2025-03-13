package utils;
import com.sun.jna.Library;
import com.sun.jna.Native;

public class MouseClicker {
	// Carrega a biblioteca User32 uma única vez
    private static final User32 user32 = User32.INSTANCE;

    public interface User32 extends Library {
        User32 INSTANCE = Native.load("user32", User32.class);
        void mouse_event(int dwFlags, int dx, int dy, int dwData, int dwExtraInfo);
    }

    // Constantes do clique do mouse
    private static final int MOUSEEVENTF_LEFTDOWN = 0x0002;
    private static final int MOUSEEVENTF_LEFTUP = 0x0004;

    public static void clicarMouse() {
        // Reutiliza a instância carregada de User32
        user32.mouse_event(MOUSEEVENTF_LEFTDOWN, 0, 0, 0, 0);
        try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        user32.mouse_event(MOUSEEVENTF_LEFTUP, 0, 0, 0, 0);
    }
}
