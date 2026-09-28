package dominio;

import java.util.Locale;
import java.util.Objects;

public final class Conta {
    private final String identificador;
    private long saldoEmCentavos;

    public Conta(String identificador, long saldoInicialEmCentavos) {
        if (identificador == null || identificador.isBlank()) {
            throw new IllegalArgumentException("O identificador da conta é obrigatório.");
        }
        if (saldoInicialEmCentavos < 0) {
            throw new IllegalArgumentException("O saldo inicial não pode ser negativo.");
        }

        this.identificador = identificador.trim();
        this.saldoEmCentavos = saldoInicialEmCentavos;
    }

    public String getIdentificador() {
        return identificador;
    }

    public long getSaldoEmCentavos() {
        return saldoEmCentavos;
    }

    public void debitar(long valorEmCentavos) {
        if (valorEmCentavos <= 0) {
            throw new IllegalArgumentException("O débito deve ser maior que zero.");
        }
        if (valorEmCentavos > saldoEmCentavos) {
            throw new IllegalStateException("Saldo insuficiente.");
        }

        saldoEmCentavos -= valorEmCentavos;
    }

    public void creditar(long valorEmCentavos) {
        if (valorEmCentavos <= 0) {
            throw new IllegalArgumentException("O crédito deve ser maior que zero.");
        }

        saldoEmCentavos = Math.addExact(saldoEmCentavos, valorEmCentavos);
    }

    @Override
    public boolean equals(Object objeto) {
        return objeto instanceof Conta outra &&
                identificador.equalsIgnoreCase(outra.identificador);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identificador.toLowerCase(Locale.ROOT));
    }
}
