package estrategia;

import dominio.Denominacao;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class BuscaComposicao {
    private BuscaComposicao() {
    }

    static Optional<Map<Denominacao, Integer>> buscarMenorQuantidadeCedulas(
            long valorEmCentavos,
            Map<Denominacao, Integer> estoque) {
        List<ItemEstoque> itens = criarItens(valorEmCentavos, estoque, true);
        if (valorEmCentavos <= 0 || itens.isEmpty()) {
            return Optional.empty();
        }

        return new BuscaMenorQuantidade(itens).executar(valorEmCentavos);
    }

    static Optional<Map<Denominacao, Integer>> buscarPreservandoCedulasMaiores(
            long valorEmCentavos,
            Map<Denominacao, Integer> estoque) {
        List<ItemEstoque> itens = criarItens(valorEmCentavos, estoque, false);
        if (valorEmCentavos <= 0 || itens.isEmpty()) {
            return Optional.empty();
        }

        return new BuscaPreservacaoMaiores(itens).executar(valorEmCentavos);
    }

    private static List<ItemEstoque> criarItens(
            long valorEmCentavos,
            Map<Denominacao, Integer> estoque,
            boolean decrescente) {
        Comparator<Map.Entry<Denominacao, Integer>> comparador =
                Map.Entry.comparingByKey();
        if (decrescente) {
            comparador = comparador.reversed();
        }

        return estoque.entrySet().stream()
                .filter(item -> item.getValue() > 0)
                .filter(item -> item.getKey().valorEmCentavos() <= valorEmCentavos)
                .sorted(comparador)
                .map(item -> new ItemEstoque(item.getKey(), item.getValue()))
                .toList();
    }

    private static long[] calcularCapacidadeRestante(List<ItemEstoque> itens) {
        long[] capacidade = new long[itens.size()];
        long totalAcumulado = 0;

        for (int indice = itens.size() - 1; indice >= 0; indice--) {
            ItemEstoque item = itens.get(indice);
            totalAcumulado = Math.addExact(
                    totalAcumulado,
                    Math.multiplyExact(item.denominacao().valorEmCentavos(), item.quantidade()));
            capacidade[indice] = totalAcumulado;
        }

        return capacidade;
    }

    private static Map<Denominacao, Integer> paraComposicao(
            List<ItemEstoque> itens,
            int[] quantidades) {
        Map<Denominacao, Integer> composicao = new LinkedHashMap<>();
        for (int indice = 0; indice < itens.size(); indice++) {
            if (quantidades[indice] > 0) {
                composicao.put(itens.get(indice).denominacao(), quantidades[indice]);
            }
        }
        return composicao;
    }

    private record ItemEstoque(Denominacao denominacao, int quantidade) {
    }

    private static final class BuscaMenorQuantidade {
        private final List<ItemEstoque> itens;
        private final long[] capacidadeRestante;
        private final int[] atual;
        private int[] melhor;
        private int melhorQuantidadeCedulas = Integer.MAX_VALUE;

        private BuscaMenorQuantidade(List<ItemEstoque> itens) {
            this.itens = new ArrayList<>(itens);
            this.capacidadeRestante = calcularCapacidadeRestante(itens);
            this.atual = new int[itens.size()];
        }

        private Optional<Map<Denominacao, Integer>> executar(long valorEmCentavos) {
            buscar(0, valorEmCentavos, 0);
            return melhor == null
                    ? Optional.empty()
                    : Optional.of(paraComposicao(itens, melhor));
        }

        private void buscar(int indice, long valorRestante, int quantidadeAtualCedulas) {
            if (valorRestante == 0) {
                if (quantidadeAtualCedulas < melhorQuantidadeCedulas) {
                    melhorQuantidadeCedulas = quantidadeAtualCedulas;
                    melhor = atual.clone();
                }
                return;
            }

            if (indice == itens.size() ||
                    quantidadeAtualCedulas >= melhorQuantidadeCedulas ||
                    valorRestante > capacidadeRestante[indice]) {
                return;
            }

            ItemEstoque item = itens.get(indice);
            int quantidadeMaxima = (int) Math.min(
                    item.quantidade(),
                    valorRestante / item.denominacao().valorEmCentavos());

            for (int quantidade = quantidadeMaxima; quantidade >= 0; quantidade--) {
                atual[indice] = quantidade;
                buscar(
                        indice + 1,
                        valorRestante - item.denominacao().valorEmCentavos() * quantidade,
                        quantidadeAtualCedulas + quantidade);
            }

            atual[indice] = 0;
        }
    }

    private static final class BuscaPreservacaoMaiores {
        private final List<ItemEstoque> itens;
        private final long[] capacidadeRestante;
        private final int[] atual;
        private int[] resultado;

        private BuscaPreservacaoMaiores(List<ItemEstoque> itens) {
            this.itens = new ArrayList<>(itens);
            this.capacidadeRestante = calcularCapacidadeRestante(itens);
            this.atual = new int[itens.size()];
        }

        private Optional<Map<Denominacao, Integer>> executar(long valorEmCentavos) {
            buscar(0, valorEmCentavos);
            return resultado == null
                    ? Optional.empty()
                    : Optional.of(paraComposicao(itens, resultado));
        }

        private boolean buscar(int indice, long valorRestante) {
            if (valorRestante == 0) {
                resultado = atual.clone();
                return true;
            }

            if (indice == itens.size() || valorRestante > capacidadeRestante[indice]) {
                return false;
            }

            ItemEstoque item = itens.get(indice);
            int quantidadeMaxima = (int) Math.min(
                    item.quantidade(),
                    valorRestante / item.denominacao().valorEmCentavos());

            // As menores denominações são consumidas primeiro para preservar as maiores.
            for (int quantidade = quantidadeMaxima; quantidade >= 0; quantidade--) {
                atual[indice] = quantidade;
                if (buscar(
                        indice + 1,
                        valorRestante - item.denominacao().valorEmCentavos() * quantidade)) {
                    return true;
                }
            }

            atual[indice] = 0;
            return false;
        }
    }
}
