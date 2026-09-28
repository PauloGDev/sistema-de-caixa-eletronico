package estrategia;

import aplicacao.contratos.EstrategiaComposicaoDinheiro;
import dominio.Denominacao;

import java.util.Map;
import java.util.Optional;

public final class EstrategiaPreservarCedulasMaiores implements EstrategiaComposicaoDinheiro {
    @Override
    public String getNome() {
        return "Preservar cédulas maiores";
    }

    @Override
    public Optional<Map<Denominacao, Integer>> compor(
            long valorSolicitadoEmCentavos,
            Map<Denominacao, Integer> estoqueDisponivel) {
        return BuscaComposicao.buscarPreservandoCedulasMaiores(
                valorSolicitadoEmCentavos,
                estoqueDisponivel);
    }

    @Override
    public String toString() {
        return getNome();
    }
}
