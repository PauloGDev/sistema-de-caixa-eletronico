package aplicacao;

import aplicacao.contratos.RepositorioContas;
import aplicacao.contratos.EstrategiaComposicaoDinheiro;
import dominio.Conta;
import dominio.InventarioDinheiro;
import dominio.Denominacao;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

public final class ServicoCaixaEletronico {
    private static final Locale BRASIL = Locale.forLanguageTag("pt-BR");

    private final RepositorioContas repositorioContas;
    private final InventarioDinheiro inventario;
    private final PublicadorEventosCaixa publicadorEventos;
    private final BuscadorSaqueAlternativo buscadorAlternativo = new BuscadorSaqueAlternativo();

    public ServicoCaixaEletronico(
            RepositorioContas repositorioContas,
            InventarioDinheiro inventario,
            PublicadorEventosCaixa publicadorEventos) {
        this.repositorioContas = repositorioContas;
        this.inventario = inventario;
        this.publicadorEventos = publicadorEventos;
    }

    public synchronized OptionalLong consultarSaldo(String identificadorConta) {
        if (identificadorConta == null || identificadorConta.isBlank()) {
            return OptionalLong.empty();
        }

        Optional<Conta> conta = repositorioContas.buscarPorIdentificador(identificadorConta);
        if (conta.isEmpty()) {
            return OptionalLong.empty();
        }

        return OptionalLong.of(conta.get().getSaldoEmCentavos());
    }

    public synchronized OptionalLong calcularValorMaximoParaSaque(String identificadorConta) {
        if (identificadorConta == null || identificadorConta.isBlank()) {
            return OptionalLong.empty();
        }

        Optional<Conta> resultadoConta =
                repositorioContas.buscarPorIdentificador(identificadorConta);
        if (resultadoConta.isEmpty()) {
            return OptionalLong.empty();
        }

        long limiteEmCentavos = Math.min(
                resultadoConta.get().getSaldoEmCentavos(),
                inventario.getValorTotalEmCentavos());

        return buscadorAlternativo.buscarMaiorValorPossivel(
                limiteEmCentavos,
                inventario.obterCopiaEstoque());
    }

    public synchronized long consultarTotalDisponivelNoCaixa() {
        return inventario.getValorTotalEmCentavos();
    }

    public synchronized Optional<ResultadoSaque> validarSolicitacaoSaque(
            String identificadorConta,
            long valorEmCentavos) {
        if (identificadorConta == null || identificadorConta.isBlank() || valorEmCentavos <= 0) {
            return Optional.of(falhar(
                    StatusSaque.REQUISICAO_INVALIDA,
                    "Informe uma conta e um valor de saque válidos."));
        }

        Optional<Conta> resultadoConta = repositorioContas.buscarPorIdentificador(identificadorConta);
        if (resultadoConta.isEmpty()) {
            return Optional.of(falhar(
                    StatusSaque.CONTA_NAO_ENCONTRADA,
                    "Conta não encontrada."));
        }

        Conta conta = resultadoConta.get();
        long saldoContaEmCentavos = conta.getSaldoEmCentavos();
        long totalCaixaEmCentavos = inventario.getValorTotalEmCentavos();
        boolean saldoContaInsuficiente = saldoContaEmCentavos < valorEmCentavos;
        boolean dinheiroCaixaInsuficiente = totalCaixaEmCentavos < valorEmCentavos;

        if (saldoContaInsuficiente && dinheiroCaixaInsuficiente) {
            return Optional.of(falhar(
                    StatusSaque.SALDO_CONTA_E_DINHEIRO_CAIXA_INSUFICIENTES,
                    "Saldo insuficiente na conta e dinheiro insuficiente no caixa. " +
                            "Saldo atual da conta: " + formatarDinheiro(saldoContaEmCentavos) +
                            "; total disponível no caixa: " + formatarDinheiro(totalCaixaEmCentavos) +
                            "; valor solicitado: " + formatarDinheiro(valorEmCentavos) + "."));
        }

        if (saldoContaInsuficiente) {
            return Optional.of(falhar(
                    StatusSaque.SALDO_CONTA_INSUFICIENTE,
                    "Saldo insuficiente na conta. Saldo atual da conta: " +
                            formatarDinheiro(saldoContaEmCentavos) +
                            "; valor solicitado: " + formatarDinheiro(valorEmCentavos) + "."));
        }

        if (dinheiroCaixaInsuficiente) {
            return Optional.of(falhar(
                    StatusSaque.DINHEIRO_CAIXA_INSUFICIENTE,
                    "Dinheiro insuficiente no caixa. Total disponível no caixa: " +
                            formatarDinheiro(totalCaixaEmCentavos) +
                            "; valor solicitado: " + formatarDinheiro(valorEmCentavos) + "."));
        }

        return Optional.empty();
    }

    public synchronized ResultadoSaque sacar(
            String identificadorConta,
            long valorEmCentavos,
            EstrategiaComposicaoDinheiro estrategiaComposicao) {
        Optional<ResultadoSaque> falhaValidacao =
                validarSolicitacaoSaque(identificadorConta, valorEmCentavos);
        if (falhaValidacao.isPresent()) {
            return falhaValidacao.get();
        }

        Conta conta = repositorioContas
                .buscarPorIdentificador(identificadorConta)
                .orElseThrow();

        Map<Denominacao, Integer> estoque = inventario.obterCopiaEstoque();
        Optional<Map<Denominacao, Integer>> resultadoComposicao =
                estrategiaComposicao.compor(valorEmCentavos, estoque);

        if (resultadoComposicao.isEmpty()) {
            OptionalLong sugestao = buscadorAlternativo.buscarValorInferiorMaisProximo(
                    valorEmCentavos,
                    conta.getSaldoEmCentavos(),
                    estoque);

            String mensagem = "O caixa possui valor total suficiente, mas não consegue compor o valor exato solicitado.";
            if (sugestao.isPresent()) {
                mensagem += " Você pode sacar " + formatarDinheiro(sugestao.getAsLong()) + ".";
            }

            publicadorEventos.publicar("Operação recusada: " + mensagem);
            return ResultadoSaque.criarFalha(
                    StatusSaque.COMPOSICAO_EXATA_INDISPONIVEL,
                    mensagem,
                    sugestao);
        }

        Map<Denominacao, Integer> composicao = resultadoComposicao.get();

        // A estratégia apenas calcula. O estado muda depois de todas as validações.
        inventario.dispensar(composicao);
        conta.debitar(valorEmCentavos);

        String valorFormatado = formatarDinheiro(valorEmCentavos);
        boolean eventoRegistrado = publicadorEventos.publicar(
                "Saque de " + valorFormatado + " realizado na conta " + conta.getIdentificador() +
                        " usando a estratégia '" + estrategiaComposicao.getNome() + "'.");

        String mensagemResultado = "Saque de " + valorFormatado + " realizado com sucesso.";
        if (!eventoRegistrado) {
            mensagemResultado += " Não foi possível registrar o evento no arquivo.";
        }

        return ResultadoSaque.criarSucesso(
                mensagemResultado,
                composicao,
                conta.getSaldoEmCentavos());
    }

    private ResultadoSaque falhar(StatusSaque situacao, String mensagem) {
        publicadorEventos.publicar("Operação recusada: " + mensagem);
        return ResultadoSaque.criarFalha(situacao, mensagem, OptionalLong.empty());
    }

    private static String formatarDinheiro(long valorEmCentavos) {
        return NumberFormat.getCurrencyInstance(BRASIL)
                .format(BigDecimal.valueOf(valorEmCentavos, 2));
    }
}
