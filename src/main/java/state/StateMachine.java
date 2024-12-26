package state;

import com.ragnarokbot.model.enums.Estado;

public class StateMachine {
	private Estado estadoAtual;

    public StateMachine(Estado estadoInicial) {
        this.estadoAtual = estadoInicial;
    }

    public Estado getEstadoAtual() {
        return estadoAtual;
    }

    public void mudarEstado(Estado novoEstado) {
        System.out.println("Mudando estado: " + estadoAtual + " -> " + novoEstado);
        this.estadoAtual = novoEstado;
    }
}
