package aplicacao.contratos;

import dominio.Denominacao;

import java.util.Map;
import java.util.Optional;

public interface EstrategiaComposicaoDinheiro {
    String getNome();

    Optional<Map<Denominacao, Integer>> compor(
            long valorSolicitadoEmCentavos,
            Map<Denominacao, Integer> estoqueDisponivel);
}
