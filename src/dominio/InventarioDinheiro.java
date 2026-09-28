package dominio;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class InventarioDinheiro {
    private final Map<Denominacao, Integer> estoque = new LinkedHashMap<>();

    public synchronized long getValorTotalEmCentavos() {
        long total = 0;
        for (Map.Entry<Denominacao, Integer> registro : estoque.entrySet()) {
            long subtotal = Math.multiplyExact(registro.getKey().valorEmCentavos(), registro.getValue());
            total = Math.addExact(total, subtotal);
        }
        return total;
    }

    public synchronized void adicionarDenominacao(Denominacao denominacao) {
        estoque.putIfAbsent(denominacao, 0);
    }

    public synchronized void removerDenominacao(Denominacao denominacao) {
        Integer quantidade = estoque.get(denominacao);
        if (quantidade == null) {
            return;
        }
        if (quantidade != 0) {
            throw new IllegalStateException(
                    "Não é possível remover uma denominação que ainda possui unidades.");
        }
        estoque.remove(denominacao);
    }

    public synchronized void carregar(Denominacao denominacao, int quantidade) {
        validarQuantidade(quantidade);
        int quantidadeAtual = estoque.getOrDefault(denominacao, 0);
        estoque.put(denominacao, Math.addExact(quantidadeAtual, quantidade));
    }

    public synchronized void descarregar(Denominacao denominacao, int quantidade) {
        validarQuantidade(quantidade);
        int quantidadeAtual = estoque.getOrDefault(denominacao, 0);
        if (quantidadeAtual < quantidade) {
            throw new IllegalStateException("Quantidade insuficiente no estoque.");
        }
        estoque.put(denominacao, quantidadeAtual - quantidade);
    }

    public synchronized Map<Denominacao, Integer> obterCopiaEstoque() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(estoque));
    }

    public synchronized void dispensar(Map<Denominacao, Integer> composicao) {
        for (Map.Entry<Denominacao, Integer> registro : composicao.entrySet()) {
            int disponivel = estoque.getOrDefault(registro.getKey(), 0);
            if (registro.getValue() <= 0 || disponivel < registro.getValue()) {
                throw new IllegalStateException("A composição não está disponível no estoque.");
            }
        }

        composicao.forEach((denominacao, quantidade) ->
                estoque.put(denominacao, estoque.get(denominacao) - quantidade));
    }

    private static void validarQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
    }
}
