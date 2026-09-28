package dominio;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/** denominação armazenada em centavos. */
public record Denominacao(long valorEmCentavos) implements Comparable<Denominacao> {
    private static final Locale BRASIL = Locale.forLanguageTag("pt-BR");

    public Denominacao {
        if (valorEmCentavos <= 0) {
            throw new IllegalArgumentException("A denominação deve ser maior que zero.");
        }
    }

    public BigDecimal valorEmReais() {
        return BigDecimal.valueOf(valorEmCentavos, 2);
    }

    @Override
    public int compareTo(Denominacao outra) {
        return Long.compare(valorEmCentavos, outra.valorEmCentavos);
    }

    @Override
    public String toString() {
        return NumberFormat.getCurrencyInstance(BRASIL).format(valorEmReais());
    }
}
