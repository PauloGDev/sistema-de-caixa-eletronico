package aplicacao;

import dominio.Denominacao;

import java.util.Map;
import java.util.OptionalLong;

public record ResultadoSaque(
        StatusSaque situacao,
        String mensagem,
        Map<Denominacao, Integer> composicao,
        OptionalLong valorSugeridoEmCentavos,
        OptionalLong saldoRestanteContaEmCentavos) {

    public ResultadoSaque {
        composicao = Map.copyOf(composicao);
    }

    public boolean sucesso() {
        return situacao == StatusSaque.SUCESSO;
    }

    public static ResultadoSaque criarSucesso(
            String mensagem,
            Map<Denominacao, Integer> composicao,
            long saldoRestanteEmCentavos) {
        return new ResultadoSaque(
                StatusSaque.SUCESSO,
                mensagem,
                composicao,
                OptionalLong.empty(),
                OptionalLong.of(saldoRestanteEmCentavos));
    }

    public static ResultadoSaque criarFalha(
            StatusSaque situacao,
            String mensagem,
            OptionalLong valorSugeridoEmCentavos) {
        return new ResultadoSaque(
                situacao,
                mensagem,
                Map.of(),
                valorSugeridoEmCentavos,
                OptionalLong.empty());
    }
}
