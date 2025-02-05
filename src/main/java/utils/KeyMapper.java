package utils;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
//notebook
public class KeyMapper {

    private static final Map<String, Integer> keyMap = new HashMap<>();

    static {
        // Carrega todas as teclas de KeyEvent na inicialização
        Field[] fields = KeyEvent.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().startsWith("VK_")) {
                try {
                    String keyName = field.getName().substring(3); // Remove "VK_"
                    int keyCode = field.getInt(null); // Valor da constante estática
                    keyMap.put(keyName, keyCode);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        
        // Adiciona modificadores manualmente (pois o KeyEvent não os tem como VK_)
        keyMap.put("CTRL", KeyEvent.VK_CONTROL);
        keyMap.put("SHIFT", KeyEvent.VK_SHIFT);
        keyMap.put("ALT", KeyEvent.VK_ALT);
    }

    /**
     * Retorna o código de evento da tecla baseada na String de atalho.
     * 
     * @param tecla A tecla como String (por exemplo, "Q", "E", etc.).
     * @return O código KeyEvent correspondente ou -1 se não encontrado.
     */
    public static int getTeclaAtalho(String tecla) {
        if (tecla == null || tecla.isEmpty()) {
            return -1; // Retorna -1 se a entrada for inválida
        }

        // Normaliza a entrada para letras maiúsculas (KeyEvent usa constantes em maiúsculas)
        String upperTecla = tecla.toUpperCase();

        // Busca o código no mapa
        return keyMap.getOrDefault(upperTecla, -1);
    }
    
    /**
     * Converte uma string de atalho (ex: "SHIFT + CTRL + P") em um array de KeyEvents.
     * 
     * @param atalho O atalho no formato "SHIFT + CTRL + P"
     * @return Um array de códigos KeyEvent, ou null se não for válido.
     */
    public static int[] getTeclaAtalhoJson(String atalho) {
        if (atalho == null || atalho.isEmpty()) {
            return new int[]{-1, 0}; // Retorna valores inválidos
        }

        String[] partes = atalho.split("\\s*\\+\\s*"); // Divide pelo "+"
        int keyCode = -1;
        int modifiers = 0;

        for (String parte : partes) {
            String upperParte = parte.toUpperCase();

            switch (upperParte) {
                case "SHIFT":
                    modifiers |= KeyEvent.SHIFT_DOWN_MASK;
                    break;
                case "CTRL":
                    modifiers |= KeyEvent.CTRL_DOWN_MASK;
                    break;
                case "ALT":
                    modifiers |= KeyEvent.ALT_DOWN_MASK;
                    break;
                default:
                    keyCode = keyMap.getOrDefault(upperParte, -1);
                    break;
            }
        }

        //System.out.println("KeyMapper: Atalho processado: " + atalho + " -> keycode=" + keyCode + ", modifiers=" + modifiers);
        return new int[]{keyCode, modifiers};
    }
}
