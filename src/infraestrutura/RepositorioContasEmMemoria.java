package infraestrutura;

import aplicacao.contratos.RepositorioContas;
import dominio.Conta;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class RepositorioContasEmMemoria implements RepositorioContas {
    private final Map<String, Conta> contas = new LinkedHashMap<>();

    @Override
    public Optional<Conta> buscarPorIdentificador(String identificadorConta) {
        if (identificadorConta == null || identificadorConta.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(contas.get(normalizar(identificadorConta)));
    }

    @Override
    public void adicionar(Conta conta) {
        String chave = normalizar(conta.getIdentificador());
        if (contas.putIfAbsent(chave, conta) != null) {
            throw new IllegalStateException("Já existe uma conta com esse identificador.");
        }
    }

    private static String normalizar(String identificadorConta) {
        return identificadorConta.trim().toLowerCase(Locale.ROOT);
    }
}
