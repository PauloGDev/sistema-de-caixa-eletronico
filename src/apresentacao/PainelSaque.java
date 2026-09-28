package apresentacao;

import aplicacao.ServicoCaixaEletronico;
import aplicacao.ResultadoSaque;
import aplicacao.contratos.EstrategiaComposicaoDinheiro;
import dominio.Denominacao;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.BiConsumer;

final class PainelSaque extends JPanel {
    private final ServicoCaixaEletronico servicoCaixaEletronico;
    private final Runnable aoSaqueBemSucedido;
    private final BiConsumer<String, Long> aoSaldoContaAtualizado;
    private String identificadorContaAtual;

    private final JLabel rotuloContaAtiva = new JLabel("—");
    private final JSpinner seletorValor = new JSpinner(
            new SpinnerNumberModel(100.00, 0.01, 1_000_000.00, 1.00));
    private final JComboBox<EstrategiaComposicaoDinheiro> comboEstrategia;
    private final JTextArea areaResultado = new JTextArea();
    private final JPanel painelResultado = new JPanel(new BorderLayout(0, 10));
    private final JPanel painelDetalhesSaque = new JPanel(new BorderLayout(0, 7));
    private final JLabel rotuloResumoCedulas = new JLabel();
    private final JLabel rotuloSaldoRestante = new JLabel();

    PainelSaque(
            ServicoCaixaEletronico servicoCaixaEletronico,
            List<EstrategiaComposicaoDinheiro> estrategias,
            Runnable aoSaqueBemSucedido,
            BiConsumer<String, Long> aoSaldoContaAtualizado) {
        this.servicoCaixaEletronico = servicoCaixaEletronico;
        this.aoSaqueBemSucedido = aoSaqueBemSucedido;
        this.aoSaldoContaAtualizado = aoSaldoContaAtualizado;
        this.comboEstrategia = new JComboBox<>(estrategias.toArray(EstrategiaComposicaoDinheiro[]::new));

        setLayout(new BorderLayout());
        setBackground(TemaInterface.FUNDO);
        setBorder(BorderFactory.createEmptyBorder(14, 10, 14, 10));

        add(criarCartaoFormulario(), BorderLayout.CENTER);
    }

    private JPanel criarCartaoFormulario() {
        JPanel cartao = new JPanel(new GridBagLayout());
        cartao.setBackground(TemaInterface.SUPERFICIE);
        cartao.setBorder(TemaInterface.bordaCartao(28));

        GridBagConstraints restricoes = new GridBagConstraints();
        restricoes.insets = new Insets(7, 7, 7, 7);
        restricoes.fill = GridBagConstraints.HORIZONTAL;
        restricoes.anchor = GridBagConstraints.WEST;

        JLabel titulo = new JLabel("Realizar saque");
        titulo.setFont(TemaInterface.FONTE_SECAO);
        titulo.setForeground(TemaInterface.TEXTO);
        restricoes.gridx = 0;
        restricoes.gridy = 0;
        restricoes.gridwidth = 2;
        restricoes.weightx = 1;
        cartao.add(titulo, restricoes);

        JLabel descricao = new JLabel(
                "Confira a conta em uso, informe o valor e escolha como as cédulas devem ser compostas.");
        descricao.setFont(TemaInterface.FONTE_CORPO);
        descricao.setForeground(TemaInterface.TEXTO_SECUNDARIO);
        restricoes.gridy = 1;
        restricoes.insets = new Insets(0, 7, 18, 7);
        cartao.add(descricao, restricoes);

        rotuloContaAtiva.setFont(TemaInterface.FONTE_ROTULO);
        rotuloContaAtiva.setForeground(TemaInterface.PRIMARIA);
        rotuloContaAtiva.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaInterface.BORDA),
                BorderFactory.createEmptyBorder(9, 10, 9, 10)));
        adicionarCampo(cartao, restricoes, 2, "Conta em uso", rotuloContaAtiva);

        seletorValor.setEditor(new JSpinner.NumberEditor(seletorValor, "#,##0.00"));
        TemaInterface.configurarCampo(seletorValor);
        adicionarCampo(cartao, restricoes, 3, "Valor do saque (R$)", seletorValor);

        TemaInterface.configurarCampo(comboEstrategia);
        adicionarCampo(cartao, restricoes, 4, "Estratégia de composição", comboEstrategia);

        JButton botaoSaqueMaximo = TemaInterface.criarBotaoSecundario("Sacar o máximo");
        botaoSaqueMaximo.setToolTipText(
                "Saca o maior valor possível considerando a conta e o estoque do caixa.");
        botaoSaqueMaximo.addActionListener(evento -> executarSaqueMaximo());
        restricoes.gridx = 0;
        restricoes.gridy = 5;
        restricoes.gridwidth = 1;
        restricoes.weightx = 0.5;
        restricoes.insets = new Insets(18, 7, 14, 7);
        cartao.add(botaoSaqueMaximo, restricoes);

        JButton botaoSaque = TemaInterface.criarBotaoPrincipal("Realizar saque");
        botaoSaque.addActionListener(evento -> executarSaque());
        restricoes.gridx = 1;
        restricoes.gridy = 5;
        restricoes.gridwidth = 1;
        restricoes.weightx = 0.5;
        restricoes.insets = new Insets(18, 7, 14, 7);
        cartao.add(botaoSaque, restricoes);

        configurarPainelResultado();
        restricoes.gridx = 0;
        restricoes.gridy = 6;
        restricoes.gridwidth = 2;
        restricoes.weighty = 1;
        restricoes.fill = GridBagConstraints.BOTH;
        restricoes.insets = new Insets(8, 7, 7, 7);
        cartao.add(painelResultado, restricoes);

        return cartao;
    }

    private void adicionarCampo(
            JPanel painel,
            GridBagConstraints restricoes,
            int linha,
            String textoRotulo,
            java.awt.Component componente) {
        JLabel rotulo = new JLabel(textoRotulo);
        rotulo.setFont(TemaInterface.FONTE_ROTULO);
        rotulo.setForeground(TemaInterface.TEXTO);

        restricoes.gridwidth = 1;
        restricoes.weighty = 0;
        restricoes.fill = GridBagConstraints.HORIZONTAL;
        restricoes.insets = new Insets(7, 7, 7, 18);
        restricoes.gridx = 0;
        restricoes.gridy = linha;
        restricoes.weightx = 0.30;
        painel.add(rotulo, restricoes);

        restricoes.gridx = 1;
        restricoes.weightx = 0.70;
        restricoes.insets = new Insets(7, 7, 7, 7);
        painel.add(componente, restricoes);
    }

    private void configurarPainelResultado() {
        painelResultado.setMinimumSize(new Dimension(450, 180));
        painelResultado.setPreferredSize(new Dimension(500, 190));
        painelResultado.setBackground(new Color(247, 249, 251));
        painelResultado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaInterface.BORDA),
                BorderFactory.createEmptyBorder(13, 15, 13, 15)));

        areaResultado.setEditable(false);
        areaResultado.setLineWrap(true);
        areaResultado.setWrapStyleWord(true);
        areaResultado.setFont(TemaInterface.FONTE_CORPO);
        areaResultado.setForeground(TemaInterface.TEXTO_SECUNDARIO);
        areaResultado.setBackground(painelResultado.getBackground());
        areaResultado.setBorder(BorderFactory.createEmptyBorder());
        areaResultado.setText("O resultado da operação aparecerá aqui.");

        JLabel tituloCedulas = new JLabel("Cédulas entregues");
        tituloCedulas.setFont(TemaInterface.FONTE_ROTULO);
        tituloCedulas.setForeground(TemaInterface.TEXTO);

        rotuloResumoCedulas.setFont(TemaInterface.FONTE_ROTULO);
        rotuloResumoCedulas.setForeground(TemaInterface.PRIMARIA);
        rotuloResumoCedulas.setBackground(Color.WHITE);
        rotuloResumoCedulas.setOpaque(true);
        rotuloResumoCedulas.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(173, 204, 224)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        JPanel linhaCedulas = new JPanel(new BorderLayout(12, 0));
        linhaCedulas.setOpaque(false);
        linhaCedulas.add(tituloCedulas, BorderLayout.WEST);
        linhaCedulas.add(rotuloResumoCedulas, BorderLayout.CENTER);

        rotuloSaldoRestante.setFont(TemaInterface.FONTE_ROTULO);
        rotuloSaldoRestante.setForeground(TemaInterface.SUCESSO);

        painelDetalhesSaque.setOpaque(false);
        painelDetalhesSaque.add(linhaCedulas, BorderLayout.CENTER);
        painelDetalhesSaque.add(rotuloSaldoRestante, BorderLayout.SOUTH);
        painelDetalhesSaque.setVisible(false);

        painelResultado.add(areaResultado, BorderLayout.NORTH);
        painelResultado.add(painelDetalhesSaque, BorderLayout.CENTER);
    }

    private void executarSaque() {
        EstrategiaComposicaoDinheiro estrategia =
                (EstrategiaComposicaoDinheiro) comboEstrategia.getSelectedItem();
        long valorEmCentavos = Math.round(
                ((Number) seletorValor.getValue()).doubleValue() * 100);

        executarSaque(valorEmCentavos, estrategia);
    }

    private void executarSaqueMaximo() {
        if (identificadorContaAtual == null) {
            exibirErro("Entre com uma conta antes de realizar o saque.");
            return;
        }

        EstrategiaComposicaoDinheiro estrategia =
                (EstrategiaComposicaoDinheiro) comboEstrategia.getSelectedItem();

        if (estrategia == null) {
            exibirErro("Selecione uma estratégia de composição.");
            return;
        }

        try {
            OptionalLong valorMaximo =
                    servicoCaixaEletronico.calcularValorMaximoParaSaque(
                            identificadorContaAtual);
            if (valorMaximo.isEmpty()) {
                exibirErro(criarMensagemSaqueMaximoIndisponivel());
                return;
            }

            seletorValor.setValue(valorMaximo.getAsLong() / 100.0);
            executarSaque(valorMaximo.getAsLong(), estrategia);
        } catch (RuntimeException excecao) {
            exibirErro("Não foi possível calcular o saque máximo: " + excecao.getMessage());
        }
    }

    private String criarMensagemSaqueMaximoIndisponivel() {
        long saldoContaEmCentavos = servicoCaixaEletronico
                .consultarSaldo(identificadorContaAtual)
                .orElse(0);
        long totalCaixaEmCentavos =
                servicoCaixaEletronico.consultarTotalDisponivelNoCaixa();

        if (saldoContaEmCentavos <= 0 && totalCaixaEmCentavos <= 0) {
            return "Não é possível sacar: a conta está sem saldo e o caixa está sem dinheiro.";
        }
        if (saldoContaEmCentavos <= 0) {
            return "Não é possível sacar: a conta está sem saldo disponível.";
        }
        if (totalCaixaEmCentavos <= 0) {
            return "Não é possível sacar: o caixa está sem dinheiro disponível.";
        }
        return "Existe saldo na conta e dinheiro no caixa, mas nenhuma composição exata " +
                "pode ser formada com as denominações disponíveis.";
    }

    private void executarSaque(
            long valorEmCentavos,
            EstrategiaComposicaoDinheiro estrategia) {
        if (identificadorContaAtual == null) {
            exibirErro("Entre com uma conta antes de realizar o saque.");
            return;
        }

        if (estrategia == null) {
            exibirErro("Selecione uma estratégia de composição.");
            return;
        }

        Optional<ResultadoSaque> falhaValidacao =
                servicoCaixaEletronico.validarSolicitacaoSaque(
                        identificadorContaAtual,
                        valorEmCentavos);
        if (falhaValidacao.isPresent()) {
            exibirErro(falhaValidacao.get().mensagem());
            return;
        }

        if (!confirmarSaque(valorEmCentavos)) {
            return;
        }

        try {
            ResultadoSaque resultado = servicoCaixaEletronico.sacar(
                    identificadorContaAtual,
                    valorEmCentavos,
                    estrategia);

            if (resultado.sucesso()) {
                exibirSucesso(resultado);
                aoSaqueBemSucedido.run();
                resultado.saldoRestanteContaEmCentavos().ifPresent(saldo ->
                        aoSaldoContaAtualizado.accept(identificadorContaAtual, saldo));
            } else {
                exibirErro(resultado.mensagem());
            }
        } catch (RuntimeException excecao) {
            exibirErro("Não foi possível concluir a operação: " + excecao.getMessage());
        }
    }

    private boolean confirmarSaque(long valorEmCentavos) {
        String mensagem = "Conta: " + identificadorContaAtual +
                "\nValor que será debitado: " +
                TemaInterface.formatarDinheiro(valorEmCentavos) +
                "\n\nDeseja confirmar o saque?";
        Object[] opcoes = {"Confirmar", "Cancelar"};

        int escolha = JOptionPane.showOptionDialog(
                this,
                mensagem,
                "Confirmar saque",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcoes,
                opcoes[1]);

        return escolha == 0;
    }

    private void exibirSucesso(ResultadoSaque resultado) {
        String resumoCedulas = resultado.composicao().entrySet().stream()
                .sorted(Map.Entry.<Denominacao, Integer>comparingByKey(
                        Comparator.reverseOrder()))
                .map(item -> {
                    long subtotal = Math.multiplyExact(
                            item.getKey().valorEmCentavos(),
                            item.getValue());
                    return item.getValue() + " × " + item.getKey() +
                            " = " + TemaInterface.formatarDinheiro(subtotal);
                })
                .reduce((primeiro, segundo) -> primeiro + "   •   " + segundo)
                .orElse("Nenhuma cédula");
        rotuloResumoCedulas.setText(resumoCedulas);

        resultado.saldoRestanteContaEmCentavos().ifPresent(saldo ->
                rotuloSaldoRestante.setText(
                        "Saldo restante na conta: " + TemaInterface.formatarDinheiro(saldo)));

        painelResultado.setBackground(TemaInterface.FUNDO_SUCESSO);
        painelResultado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(160, 211, 183)),
                BorderFactory.createEmptyBorder(13, 15, 13, 15)));
        areaResultado.setForeground(TemaInterface.SUCESSO);
        areaResultado.setBackground(TemaInterface.FUNDO_SUCESSO);
        areaResultado.setText("Saque aprovado\n" + resultado.mensagem());
        areaResultado.setCaretPosition(0);
        painelDetalhesSaque.setVisible(true);
        revalidate();
        repaint();
    }

    private void exibirErro(String mensagem) {
        rotuloResumoCedulas.setText("");
        rotuloSaldoRestante.setText("");
        painelDetalhesSaque.setVisible(false);
        painelResultado.setBackground(TemaInterface.FUNDO_ERRO);
        painelResultado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(231, 177, 177)),
                BorderFactory.createEmptyBorder(13, 15, 13, 15)));
        areaResultado.setForeground(TemaInterface.ERRO);
        areaResultado.setBackground(TemaInterface.FUNDO_ERRO);
        areaResultado.setText(mensagem);
        areaResultado.setCaretPosition(0);
        revalidate();
        repaint();
    }

    void iniciarSessao(String identificadorConta) {
        identificadorContaAtual = identificadorConta;
        rotuloContaAtiva.setText(identificadorConta);
        limparResultado();
    }

    void encerrarSessao() {
        identificadorContaAtual = null;
        rotuloContaAtiva.setText("—");
        seletorValor.setValue(100.00);
        if (comboEstrategia.getItemCount() > 0) {
            comboEstrategia.setSelectedIndex(0);
        }
        limparResultado();
    }

    private void limparResultado() {
        rotuloResumoCedulas.setText("");
        rotuloSaldoRestante.setText("");
        painelDetalhesSaque.setVisible(false);
        painelResultado.setBackground(new Color(247, 249, 251));
        painelResultado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaInterface.BORDA),
                BorderFactory.createEmptyBorder(13, 15, 13, 15)));
        areaResultado.setForeground(TemaInterface.TEXTO_SECUNDARIO);
        areaResultado.setBackground(painelResultado.getBackground());
        areaResultado.setText("O resultado da operação aparecerá aqui.");
        revalidate();
        repaint();
    }

}
