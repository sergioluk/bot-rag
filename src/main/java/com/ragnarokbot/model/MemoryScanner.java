package com.ragnarokbot.model;

import java.util.ArrayList;
import java.util.List;

import com.ragnarokbot.model.MemoryScanner.Kernel32;
import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference; // Classe para manipular valores inteiros por referência.
import com.sun.jna.ptr.PointerByReference; // Se necessário para ponteiros.

public class MemoryScanner {
	
	public static int processId = 0; 

	//public static long addressX = 0x156FD4C; //history
	//public static long addressX = 0xE77288; //tales
	public static long addressX = 0;

	//public static long addressY = 0x156FD4C + 4;  //history
	//public static long addressY = 0xE77288 + 4;//tales
	public static long addressY = 0;

	//public long addressHp = 0x15874D0; //history
	//public static long addressHp = 0x00E8E434; //tales
	public static long addressHp = 0;
	
	public static long addressString = 0x19A9ED - 5;
	
	//public static long addressMapa = 0x1583574;   //history
	//public static long addressMapa = 0xE89BD4;  //tales
	public static long addressMapa = 0;
	
	//public static long addressName = 0x0158A120; //history
	//public static long addressName = 0x00E90C00; //tales
	public static long addressName = 0;
	
	
	
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
    
  //notebook
    public int obterHP(int processId, long addressHP) {
        Pointer processHandle = Kernel32.INSTANCE.OpenProcess(
                Kernel32.PROCESS_VM_READ | Kernel32.PROCESS_QUERY_INFORMATION,
                false,
                processId
        );

        if (processHandle == null) {
            System.err.println("Não foi possível abrir o processo.");
            return -1; // Retorna -1 para indicar erro
        }

        byte[] bufferHP = new byte[4]; // Buffer para o valor de HP (4 bytes para um int)
        IntByReference bytesRead = new IntByReference();

        try {
            boolean successHP = Kernel32.INSTANCE.ReadProcessMemory(
                    processHandle,
                    new Pointer(addressHP),
                    bufferHP,
                    bufferHP.length,
                    bytesRead
            );

            if (successHP && bytesRead.getValue() == 4) {
                // Converte o buffer para um inteiro
                return java.nio.ByteBuffer.wrap(bufferHP).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
            } else {
                System.err.println("Erro ao ler memória para HP.");
                return -1;
            }

        } finally {
            Kernel32.INSTANCE.CloseHandle(processHandle);
        }
    }
    
    public static String obterStringMemoria(int processId, long addressString) {
        Pointer processHandle = Kernel32.INSTANCE.OpenProcess(
                Kernel32.PROCESS_VM_READ | Kernel32.PROCESS_QUERY_INFORMATION,
                false,
                processId
        );

        if (processHandle == null) {
            System.err.println("Não foi possível abrir o processo.");
            return null;
        }

        byte[] buffer = new byte[256]; // Tamanho máximo da string a ser lida
        IntByReference bytesRead = new IntByReference();

        try {
            boolean success = Kernel32.INSTANCE.ReadProcessMemory(
                    processHandle,
                    new Pointer(addressString),
                    buffer,
                    buffer.length,
                    bytesRead
            );

            if (success && bytesRead.getValue() > 0) {
                // Converter bytes para String, cortando no primeiro '\0' (caso seja uma string C-style)
                return new String(buffer, 0, bytesRead.getValue(), "UTF-8").split("\0")[0];
            } else {
                System.err.println("Erro ao ler memória para a string.");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar string: " + e.getMessage());
            return null;
        } finally {
            Kernel32.INSTANCE.CloseHandle(processHandle);
        }
    }
    
    public String obterMapa() {
        Pointer processHandle = Kernel32.INSTANCE.OpenProcess(
                Kernel32.PROCESS_VM_READ | Kernel32.PROCESS_QUERY_INFORMATION,
                false,
                processId
        );

        if (processHandle == null) {
            System.err.println("Não foi possível abrir o processo.");
            return null;
        }

        byte[] buffer = new byte[256]; // Tamanho máximo da string a ser lida
        IntByReference bytesRead = new IntByReference();

        try {
            boolean success = Kernel32.INSTANCE.ReadProcessMemory(
                    processHandle,
                    new Pointer(addressMapa),
                    buffer,
                    buffer.length,
                    bytesRead
            );

            if (success && bytesRead.getValue() > 0) {
                // Converter bytes para String, cortando no primeiro '\0' (caso seja uma string C-style)
                return new String(buffer, 0, bytesRead.getValue(), "UTF-8").split("\0")[0];
            } else {
                System.err.println("Erro ao ler memória para a string.");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar string: " + e.getMessage());
            return null;
        } finally {
            Kernel32.INSTANCE.CloseHandle(processHandle);
        }
    }
    
    public List<Integer> listarStatus() {
        List<Integer> buffs = new ArrayList<>();

        Pointer processHandle = Kernel32.INSTANCE.OpenProcess(
                Kernel32.PROCESS_VM_READ | Kernel32.PROCESS_QUERY_INFORMATION,
                false,
                processId
        );

        if (processHandle == null) {
            System.err.println("Não foi possível abrir o processo.");
            return buffs; // Retorna lista vazia se não puder abrir o processo
        }

        byte[] buffer = new byte[4]; // Buffer de 4 bytes para armazenar cada buff
        IntByReference bytesRead = new IntByReference();

        try {
            for (int i = 0; i < 100; i++) {
                long currentAddress = addressHp + 0x474 + i * 4; // Calcula o endereço do buff
                
                boolean success = Kernel32.INSTANCE.ReadProcessMemory(
                        processHandle,
                        new Pointer(currentAddress),
                        buffer,
                        buffer.length,
                        bytesRead
                );

                if (!success || bytesRead.getValue() != 4) {
                    break; // Para de adicionar se falhar na leitura
                }

                int buff = java.nio.ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
                
                if (buff != -1) {
                    buffs.add(buff);
                }
            }
        } finally {
            Kernel32.INSTANCE.CloseHandle(processHandle);
        }

        return buffs;
    }

	public static long getAddressX() {
		return addressX;
	}

	public static void setAddressX(long addressX) {
		MemoryScanner.addressX = addressX;
	}

	public static long getAddressY() {
		return addressY;
	}

	public static void setAddressY(long addressY) {
		MemoryScanner.addressY = addressY;
	}

	public static long getAddressHp() {
		return addressHp;
	}

	public static void setAddressHp(long addressHp) {
		MemoryScanner.addressHp = addressHp;
	}

	public static long getAddressString() {
		return addressString;
	}

	public static void setAddressString(long addressString) {
		MemoryScanner.addressString = addressString;
	}

	public static long getAddressMapa() {
		return addressMapa;
	}

	public static void setAddressMapa(long addressMapa) {
		MemoryScanner.addressMapa = addressMapa;
	}

	public static long getAddressName() {
		return addressName;
	}

	public static void setAddressName(long addressName) {
		MemoryScanner.addressName = addressName;
	}

    

}