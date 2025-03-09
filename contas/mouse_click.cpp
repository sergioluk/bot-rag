#include <windows.h>

// Exporta a função para que possa ser usada na DLL
extern "C" __declspec(dllexport) void clicarMouse() {
    // Pressiona o botão esquerdo do mouse
    mouse_event(MOUSEEVENTF_LEFTDOWN, 0, 0, 0, 0);
    Sleep(50);  // Aguarda 100ms
    // Solta o botão esquerdo do mouse
    mouse_event(MOUSEEVENTF_LEFTUP, 0, 0, 0, 0);
}
