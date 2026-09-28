package dominio;

import java.time.OffsetDateTime;
import java.util.Objects;

public record EventoCaixa(OffsetDateTime ocorridoEm, String mensagem) {
    public EventoCaixa {
        Objects.requireNonNull(ocorridoEm, "A data do evento é obrigatória.");
        if (mensagem == null || mensagem.isBlank()) {
            throw new IllegalArgumentException("A mensagem do evento é obrigatória.");
        }
    }
}
