package estrategia;

import aplicacao.contratos.EstrategiaComposicaoDinheiro;
import dominio.Denominacao;

import java.util.Map;
import java.util.Optional;

public final class EstrategiaMenorQuantidadeCedulas implements EstrategiaComposicaoDinheiro {
    @Override
    public String getNome() {
        return "Menor quantidade de cédulas";
    }

    @Override
    public Optional<Map<Denominacao, Integer>> compor(
            long valorSolicitadoEmCentavos,
            Map<Denominacao, Integer> estoqueDisponivel) {
        return BuscaComposicao.buscarMenorQuantidadeCedulas(valorSolicitadoEmCentavos, estoqueDisponivel);
    }

    @Override
    public String toString() {
        return getNome();
    }
}
