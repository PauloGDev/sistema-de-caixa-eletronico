package configuracao;

import aplicacao.ServicoCaixaEletronico;
import aplicacao.ServicoInventario;
import aplicacao.contratos.EstrategiaComposicaoDinheiro;
import dominio.InventarioDinheiro;

import java.util.List;

public record DependenciasAplicacao(
        ServicoCaixaEletronico servicoCaixaEletronico,
        ServicoInventario servicoInventario,
        InventarioDinheiro inventario,
        List<EstrategiaComposicaoDinheiro> estrategias) {

    public DependenciasAplicacao {
        estrategias = List.copyOf(estrategias);
    }
}
