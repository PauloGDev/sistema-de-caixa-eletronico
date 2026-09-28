package aplicacao;

import dominio.InventarioDinheiro;
import dominio.Denominacao;

public final class ServicoInventario {
    private final InventarioDinheiro inventario;
    private final PublicadorEventosCaixa publicadorEventos;

    public ServicoInventario(InventarioDinheiro inventario, PublicadorEventosCaixa publicadorEventos) {
        this.inventario = inventario;
        this.publicadorEventos = publicadorEventos;
    }

    public boolean carregar(long denominacaoEmCentavos, int quantidade) {
        Denominacao denominacao = new Denominacao(denominacaoEmCentavos);
        inventario.carregar(denominacao, quantidade);
        return publicadorEventos.publicar(
                "Carregadas " + quantidade + " unidade(s) de " + denominacao + ".");
    }

    public boolean descarregar(long denominacaoEmCentavos, int quantidade) {
        Denominacao denominacao = new Denominacao(denominacaoEmCentavos);
        inventario.descarregar(denominacao, quantidade);
        return publicadorEventos.publicar(
                "Descarregadas " + quantidade + " unidade(s) de " + denominacao + ".");
    }
}
