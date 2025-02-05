package utils;

import java.util.HashSet;
import java.util.Set;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

public class Atalho {
    private int keyCode;
    private Set<Integer> modifiers;

    public Atalho(int keyCode, Set<Integer> modifiers) {
        this.keyCode = keyCode;
        this.modifiers = modifiers;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public Set<Integer> getModifiers() {
        return modifiers;
    }

    public boolean comparaAtalho(int keyCode, int modifiers) {
        return this.keyCode == keyCode && this.modifiers.equals(extrairModifiers(modifiers));
    }

    public static Set<Integer> extrairModifiers(int modifiers) {
        Set<Integer> result = new HashSet<>();
        if ((modifiers & NativeKeyEvent.CTRL_MASK) != 0) result.add(NativeKeyEvent.CTRL_MASK);
        if ((modifiers & NativeKeyEvent.SHIFT_MASK) != 0) result.add(NativeKeyEvent.SHIFT_MASK);
        if ((modifiers & NativeKeyEvent.ALT_MASK) != 0) result.add(NativeKeyEvent.ALT_MASK);
        return result;
    }
}
