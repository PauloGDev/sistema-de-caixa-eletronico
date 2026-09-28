package infraestrutura;

import aplicacao.contratos.NotificadorCaixa;
import dominio.EventoCaixa;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;

public final class NotificadorArquivo implements NotificadorCaixa {
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss XXX");

    private final Path caminhoArquivo;

    public NotificadorArquivo(Path caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
    }

    @Override
    public synchronized void notificar(EventoCaixa evento) {
        try {
            Path diretorioPai = caminhoArquivo.getParent();
            if (diretorioPai != null) {
                Files.createDirectories(diretorioPai);
            }

            String linha = FORMATO_DATA.format(evento.ocorridoEm()) +
                    " | " + evento.mensagem() + System.lineSeparator();

            Files.writeString(
                    caminhoArquivo,
                    linha,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException excecao) {
            throw new UncheckedIOException("Não foi possível registrar o evento.", excecao);
        }
    }
}
