package aplicacao;

import aplicacao.contratos.NotificadorCaixa;
import dominio.EventoCaixa;

import java.time.OffsetDateTime;
import java.util.List;

public final class PublicadorEventosCaixa {
    private final List<NotificadorCaixa> notificadores;

    public PublicadorEventosCaixa(List<NotificadorCaixa> notificadores) {
        this.notificadores = List.copyOf(notificadores);
    }

    public boolean publicar(String mensagem) {
        EventoCaixa evento = new EventoCaixa(OffsetDateTime.now(), mensagem);
        boolean todosNotificados = true;

        for (NotificadorCaixa notificador : notificadores) {
            try {
                notificador.notificar(evento);
            } catch (RuntimeException excecao) {
                todosNotificados = false;
            }
        }

        return todosNotificados;
    }
}
