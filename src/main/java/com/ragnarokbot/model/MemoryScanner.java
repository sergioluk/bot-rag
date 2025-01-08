package com.ragnarokbot.model;

import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference; // Classe para manipular valores inteiros por referência.
import com.sun.jna.ptr.PointerByReference; // Se necessário para ponteiros.

public class MemoryScanner {
	
	public static int processId = 0; 
	//public long addressX = 0x19D468; 
	public long addressX = 0x1557FBC;
	//public long addressY = 0x19D46C; 
	public long addressY = 0x1557FC0;
	
    public interface Kernel32 extends Library {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);

        int PROCESS_VM_READ = 0x0010;
        int PROCESS_QUERY_INFORMATION = 0x0400; // Permissão para consultar informações do processo.


        Pointer OpenProcess(int dwDesiredAccess, boolean bInheritHandle, int dwProcessId);
        boolean ReadProcessMemory(Pointer hProcess, Pointer lpBaseAddress, byte[] lpBuffer, int nSize, IntByReference lpNumberOfBytesRead);
        boolean CloseHandle(Pointer hObject);
    }
    
 // Método que retorna as coordenadas (X, Y)
    public Coordenadas obterCoordenadas(int processId, long addressX, long addressY) {
        Pointer processHandle = Kernel32.INSTANCE.OpenProcess(
                Kernel32.PROCESS_VM_READ | Kernel32.PROCESS_QUERY_INFORMATION,
                false,
                processId
        );

        if (processHandle == null) {
            System.err.println("Não foi possível abrir o processo.");
            return null;
        }

        // Buffers para leitura das coordenadas
        byte[] bufferX = new byte[4]; // Buffer para o valor de X (4 bytes para um int)
        byte[] bufferY = new byte[4]; // Buffer para o valor de Y (4 bytes para um int)
        IntByReference bytesRead = new IntByReference();

        try {
            // Leia a memória para as coordenadas X e Y
            boolean successX = Kernel32.INSTANCE.ReadProcessMemory(
                    processHandle,
                    new Pointer(addressX),
                    bufferX,
                    bufferX.length,
                    bytesRead
            );

            boolean successY = Kernel32.INSTANCE.ReadProcessMemory(
                    processHandle,
                    new Pointer(addressY),
                    bufferY,
                    bufferY.length,
                    bytesRead
            );

            if (successX && successY && bytesRead.getValue() == 4) {
                // Converta os buffers para valores inteiros
                int x = java.nio.ByteBuffer.wrap(bufferX).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
                int y = java.nio.ByteBuffer.wrap(bufferY).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();

                // Retorne as coordenadas como um objeto Coordenada
                return new Coordenadas(x, y);
            } else {
                System.err.println("Erro ao ler memória.");
                return null;
            }

        } finally {
            // Feche o handle do processo
            Kernel32.INSTANCE.CloseHandle(processHandle);
        }
    }

}