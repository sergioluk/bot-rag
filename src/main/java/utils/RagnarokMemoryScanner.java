package utils;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;

public class RagnarokMemoryScanner {

    private static final int PROCESS_VM_READ = 0x0010;
    private static final int PROCESS_QUERY_INFORMATION = 0x0400;
    private static final int BUFFER_SIZE = 4096; // Tamanho do bloco de leitura (4KB)

    public static void scanStringsInMemory(int processId, long startAddress, long endAddress) {
        WinNT.HANDLE processHandle = Kernel32.INSTANCE.OpenProcess(
                PROCESS_VM_READ | PROCESS_QUERY_INFORMATION,
                false,
                processId
        );

        if (processHandle == null || WinNT.INVALID_HANDLE_VALUE.equals(processHandle)) {
            System.err.println("Erro: Não foi possível abrir o processo.");
            return;
        }

        Memory buffer = new Memory(BUFFER_SIZE);
        IntByReference bytesRead = new IntByReference();

        System.out.println("🔍 Escaneando memória do processo " + processId + " em busca de strings...");

        for (long address = startAddress; address < endAddress; address += BUFFER_SIZE) {
            boolean success = Kernel32.INSTANCE.ReadProcessMemory(
                    processHandle,
                    new Pointer(address),
                    buffer,
                    BUFFER_SIZE,
                    bytesRead
            );

            if (success && bytesRead.getValue() > 0) {
                extractAndPrintStrings(buffer.getByteArray(0, bytesRead.getValue()), address);
            }

            // 🔹 Adiciona um delay de 100ms entre cada leitura para evitar sobrecarga do sistema
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.err.println("Leitura interrompida.");
                break;
            }
        }

        Kernel32.INSTANCE.CloseHandle(processHandle);
        System.out.println("✅ Escaneamento concluído.");
    }

    private static void extractAndPrintStrings(byte[] buffer, long baseAddress) {
        String rawString = new String(buffer);
        String[] possibleStrings = rawString.split("\0");

        for (String str : possibleStrings) {
            if (str.length() > 3 && str.matches("[ -~]+")) { // Strings legíveis (ASCII)
                System.out.printf("📍 Endereço: 0x%X -> String: \"%s\"%n", baseAddress, str);
            }
        }
    }

    public static void main(String[] args) {
        int processId = 1234; // 🔹 Substitua pelo PID real do Ragnarok
        long startMemory = 0x00010000; // 🔹 Endereço inicial da memória
        long endMemory = 0x7FFFFFFF;   // 🔹 Endereço final (ajuste se necessário)

        scanStringsInMemory(processId, startMemory, endMemory);
    }
}
