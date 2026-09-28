package configuracao;

import aplicacao.PublicadorEventosCaixa;
import aplicacao.ServicoCaixaEletronico;
import aplicacao.ServicoInventario;
import aplicacao.contratos.NotificadorCaixa;
import aplicacao.contratos.EstrategiaComposicaoDinheiro;
import dominio.Conta;
import dominio.InventarioDinheiro;
import dominio.Denominacao;
import infraestrutura.NotificadorArquivo;
import infraestrutura.RepositorioContasEmMemoria;
import estrategia.EstrategiaMenorQuantidadeCedulas;
import estrategia.EstrategiaPreservarCedulasMaiores;

import java.nio.file.Path;
import java.util.List;

public final class ConfiguracaoAplicacao {
    private ConfiguracaoAplicacao() {
    }

    public static DependenciasAplicacao criar() {
        InventarioDinheiro inventario = criarInventario();

        RepositorioContasEmMemoria repositorioContas = new RepositorioContasEmMemoria();
        repositorioContas.adicionar(new Conta("123456", 500_000));

        Path caminhoLog = Path.of("registros", "eventos-caixa.txt").toAbsolutePath();
        List<NotificadorCaixa> notificadores = List.of(new NotificadorArquivo(caminhoLog));
        PublicadorEventosCaixa publicadorEventos = new PublicadorEventosCaixa(notificadores);

        List<EstrategiaComposicaoDinheiro> estrategias = List.of(
                new EstrategiaMenorQuantidadeCedulas(),
                new EstrategiaPreservarCedulasMaiores());

        return new DependenciasAplicacao(
                new ServicoCaixaEletronico(repositorioContas, inventario, publicadorEventos),
                new ServicoInventario(inventario, publicadorEventos),
                inventario,
                estrategias);
    }

    private static InventarioDinheiro criarInventario() {
        InventarioDinheiro inventario = new InventarioDinheiro();
        long[] valoresDenominacoesEmCentavos = {
                20_000,
                10_000,
                5_000,
                2_000,
                1_000,
                500,
                200,
                100
        };

        for (long valorEmCentavos : valoresDenominacoesEmCentavos) {
            inventario.carregar(new Denominacao(valorEmCentavos), 10);
        }

        return inventario;
    }
}
