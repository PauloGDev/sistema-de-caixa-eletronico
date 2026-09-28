package aplicacao.contratos;

import dominio.EventoCaixa;

public interface NotificadorCaixa {
    void notificar(EventoCaixa evento);
}
