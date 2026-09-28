package aplicacao;

import dominio.Denominacao;

import java.util.Arrays;
import java.util.Map;
import java.util.OptionalLong;

final class BuscadorSaqueAlternativo {
    private static final int MAXIMO_UNIDADES_BUSCA = 2_000_000;

    OptionalLong buscarValorInferiorMaisProximo(
            long valorSolicitadoEmCentavos,
            long saldoContaEmCentavos,
            Map<Denominacao, Integer> estoque) {
        long totalEstoqueEmCentavos = estoque.entrySet().stream()
                .mapToLong(item -> item.getKey().valorEmCentavos() * item.getValue())
                .sum();

        long candidatoMaximo = Math.min(
                valorSolicitadoEmCentavos - 1,
                Math.min(saldoContaEmCentavos, totalEstoqueEmCentavos));

        return buscarMaiorValorPossivel(candidatoMaximo, estoque);
    }

    OptionalLong buscarMaiorValorPossivel(
            long limiteEmCentavos,
            Map<Denominacao, Integer> estoque) {
        if (limiteEmCentavos <= 0) {
            return OptionalLong.empty();
        }

        long passo = estoque.entrySet().stream()
                .filter(item -> item.getValue() > 0)
                .mapToLong(item -> item.getKey().valorEmCentavos())
                .reduce(this::maximoDivisorComum)
                .orElse(1);

        long candidatoMaximo = limiteEmCentavos - limiteEmCentavos % passo;
        long maximoUnidadesComoLong = candidatoMaximo / passo;
        if (maximoUnidadesComoLong <= 0 || maximoUnidadesComoLong > MAXIMO_UNIDADES_BUSCA) {
            return OptionalLong.empty();
        }

        int maximoUnidades = (int) maximoUnidadesComoLong;
        int[] usosRestantes = new int[maximoUnidades + 1];
        Arrays.fill(usosRestantes, -1);
        usosRestantes[0] = 0;

        for (Map.Entry<Denominacao, Integer> item : estoque.entrySet()) {
            if (item.getValue() <= 0) {
                continue;
            }

            int unidadesDenominacao = Math.toIntExact(item.getKey().valorEmCentavos() / passo);
            for (int valor = 0; valor <= maximoUnidades; valor++) {
                if (usosRestantes[valor] >= 0) {
                    usosRestantes[valor] = item.getValue();
                } else if (valor < unidadesDenominacao ||
                        usosRestantes[valor - unidadesDenominacao] <= 0) {
                    usosRestantes[valor] = -1;
                } else {
                    usosRestantes[valor] = usosRestantes[valor - unidadesDenominacao] - 1;
                }
            }
        }

        for (int valor = maximoUnidades; valor > 0; valor--) {
            if (usosRestantes[valor] >= 0) {
                return OptionalLong.of(valor * passo);
            }
        }

        return OptionalLong.empty();
    }

    private long maximoDivisorComum(long primeiro, long segundo) {
        long esquerdo = Math.abs(primeiro);
        long direito = Math.abs(segundo);
        while (direito != 0) {
            long resto = esquerdo % direito;
            esquerdo = direito;
            direito = resto;
        }
        return esquerdo;
    }
}
