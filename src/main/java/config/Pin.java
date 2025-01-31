package config;

import java.util.List;
import java.util.Map;

public class Pin {
    private List<Map<String, String>> pins;

    public List<Map<String, String>> getPins() {
        return pins;
    }

    public void setPins(List<Map<String, String>> pins) {
        this.pins = pins;
    }
}

