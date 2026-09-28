package apresentacao;

import aplicacao.ServicoCaixaEletronico;
import configuracao.DependenciasAplicacao;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.OptionalLong;

public final class JanelaPrincipal extends JFrame {
    private static final String TELA_ACESSO = "acesso";
    private static final String TELA_SISTEMA = "sistema";

    private final ServicoCaixaEletronico servicoCaixaEletronico;
    private final CardLayout gerenciadorTelas = new CardLayout();
    private final JPanel painelTelas = new JPanel(gerenciadorTelas);
    private final JTextField campoContaAcesso = new JTextField();
    private final JLabel rotuloMensagemAcesso = new JLabel(" ");
    private final JLabel rotuloContaSaldo = new JLabel(
            "Conta utilizada: nenhuma  |  Saldo atual da conta: —");
    private final JLabel rotuloSituacao = new JLabel("Sistema disponível");
    private final PainelInventario painelInventario;
    private final PainelSaque painelSaque;

    public JanelaPrincipal(DependenciasAplicacao dependencias) {
        super("Sistema de Caixa Eletrônico");

        servicoCaixaEletronico = dependencias.servicoCaixaEletronico();
        painelInventario = new PainelInventario(
                dependencias.servicoInventario(),
                dependencias.inventario());
        painelSaque = new PainelSaque(
                servicoCaixaEletronico,
                dependencias.estrategias(),
                painelInventario::atualizarDados,
                this::atualizarRodapeConta);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 720));
        setSize(980, 790);
        setLocationRelativeTo(null);

        painelTelas.add(criarTelaAcesso(), TELA_ACESSO);
        painelTelas.add(criarTelaSistema(), TELA_SISTEMA);
        setContentPane(painelTelas);
        gerenciadorTelas.show(painelTelas, TELA_ACESSO);

        SwingUtilities.invokeLater(campoContaAcesso::requestFocusInWindow);
    }

    private JPanel criarTelaAcesso() {
        JPanel tela = new JPanel(new BorderLayout());
        tela.setBackground(TemaInterface.FUNDO);

        JPanel faixaSuperior = new JPanel();
        faixaSuperior.setLayout(new BoxLayout(faixaSuperior, BoxLayout.Y_AXIS));
        faixaSuperior.setBackground(TemaInterface.PRIMARIA);
        faixaSuperior.setBorder(BorderFactory.createEmptyBorder(28, 34, 28, 34));

        JLabel tituloSistema = new JLabel("Caixa Eletrônico");
        tituloSistema.setFont(TemaInterface.FONTE_TITULO);
        tituloSistema.setForeground(java.awt.Color.WHITE);

        JLabel subtituloSistema = new JLabel("Acesso seguro à conta");
        subtituloSistema.setFont(TemaInterface.FONTE_CORPO);
        subtituloSistema.setForeground(new java.awt.Color(213, 228, 239));

        faixaSuperior.add(tituloSistema);
        faixaSuperior.add(Box.createVerticalStrut(4));
        faixaSuperior.add(subtituloSistema);

        JPanel centralizador = new JPanel(new GridBagLayout());
        centralizador.setOpaque(false);
        centralizador.setBorder(BorderFactory.createEmptyBorder(35, 25, 35, 25));

        JPanel cartao = new JPanel(new GridBagLayout());
        cartao.setBackground(TemaInterface.SUPERFICIE);
        cartao.setBorder(TemaInterface.bordaCartao(32));
        cartao.setPreferredSize(new Dimension(480, 350));

        GridBagConstraints restricoes = new GridBagConstraints();
        restricoes.gridx = 0;
        restricoes.fill = GridBagConstraints.HORIZONTAL;
        restricoes.weightx = 1;

        JLabel titulo = new JLabel("Identifique sua conta");
        titulo.setFont(TemaInterface.FONTE_SECAO);
        titulo.setForeground(TemaInterface.TEXTO);
        restricoes.gridy = 0;
        restricoes.insets = new Insets(0, 0, 8, 0);
        cartao.add(titulo, restricoes);

        JLabel descricao = new JLabel(
                "<html>Informe o identificador para acessar as operações do caixa eletrônico.</html>");
        descricao.setFont(TemaInterface.FONTE_CORPO);
        descricao.setForeground(TemaInterface.TEXTO_SECUNDARIO);
        restricoes.gridy = 1;
        restricoes.insets = new Insets(0, 0, 24, 0);
        cartao.add(descricao, restricoes);

        JLabel rotuloConta = new JLabel("Identificador da conta");
        rotuloConta.setFont(TemaInterface.FONTE_ROTULO);
        rotuloConta.setForeground(TemaInterface.TEXTO);
        restricoes.gridy = 2;
        restricoes.insets = new Insets(0, 0, 7, 0);
        cartao.add(rotuloConta, restricoes);

        TemaInterface.configurarCampo(campoContaAcesso);
        campoContaAcesso.setPreferredSize(new Dimension(360, 42));
        campoContaAcesso.addActionListener(evento -> entrar());
        restricoes.gridy = 3;
        restricoes.insets = new Insets(0, 0, 8, 0);
        cartao.add(campoContaAcesso, restricoes);

        rotuloMensagemAcesso.setFont(TemaInterface.FONTE_CORPO);
        rotuloMensagemAcesso.setForeground(TemaInterface.ERRO);
        restricoes.gridy = 4;
        restricoes.insets = new Insets(0, 0, 14, 0);
        cartao.add(rotuloMensagemAcesso, restricoes);

        JButton botaoEntrar = TemaInterface.criarBotaoPrincipal("Entrar");
        botaoEntrar.addActionListener(evento -> entrar());
        restricoes.gridy = 5;
        restricoes.insets = new Insets(0, 0, 18, 0);
        cartao.add(botaoEntrar, restricoes);

        centralizador.add(cartao);
        tela.add(faixaSuperior, BorderLayout.NORTH);
        tela.add(centralizador, BorderLayout.CENTER);
        return tela;
    }

    private JPanel criarTelaSistema() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(TemaInterface.FUNDO);

        JTabbedPane abas = new JTabbedPane();
        abas.setFont(TemaInterface.FONTE_ROTULO);
        abas.setBorder(BorderFactory.createEmptyBorder(16, 26, 24, 26));
        abas.addTab("Saque", painelSaque);
        abas.addTab("Administração do estoque", painelInventario);

        raiz.add(criarCabecalho(), BorderLayout.NORTH);
        raiz.add(abas, BorderLayout.CENTER);
        raiz.add(criarRodape(), BorderLayout.SOUTH);
        return raiz;
    }

    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout(20, 0));
        cabecalho.setBackground(TemaInterface.PRIMARIA);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(22, 30, 22, 30));

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setOpaque(false);

        JLabel titulo = new JLabel("Caixa Eletrônico");
        titulo.setFont(TemaInterface.FONTE_TITULO);
        titulo.setForeground(java.awt.Color.WHITE);

        JLabel subtitulo = new JLabel("Saques inteligentes e controle de numerário");
        subtitulo.setFont(TemaInterface.FONTE_CORPO);
        subtitulo.setForeground(new java.awt.Color(213, 228, 239));

        textos.add(titulo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(subtitulo);

        JButton botaoSair = TemaInterface.criarBotaoPerigo("Sair");
        botaoSair.addActionListener(evento -> sair());

        cabecalho.add(textos, BorderLayout.CENTER);
        cabecalho.add(botaoSair, BorderLayout.EAST);
        return cabecalho;
    }

    private JPanel criarRodape() {
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBackground(TemaInterface.SUPERFICIE);
        rodape.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, TemaInterface.BORDA),
                BorderFactory.createEmptyBorder(10, 28, 10, 28)));

        rotuloContaSaldo.setFont(TemaInterface.FONTE_CORPO);
        rotuloContaSaldo.setForeground(TemaInterface.TEXTO_SECUNDARIO);

        rotuloSituacao.setFont(TemaInterface.FONTE_ROTULO);
        rotuloSituacao.setForeground(TemaInterface.SUCESSO);

        rodape.add(rotuloContaSaldo, BorderLayout.WEST);
        rodape.add(rotuloSituacao, BorderLayout.EAST);
        return rodape;
    }

    private void entrar() {
        String identificadorConta = campoContaAcesso.getText().trim();
        if (identificadorConta.isEmpty()) {
            exibirErroAcesso("Informe o identificador da conta.");
            return;
        }

        OptionalLong saldoAtual = servicoCaixaEletronico.consultarSaldo(identificadorConta);
        if (saldoAtual.isEmpty()) {
            exibirErroAcesso("Conta não encontrada. Verifique o identificador informado.");
            return;
        }

        rotuloMensagemAcesso.setText(" ");
        painelSaque.iniciarSessao(identificadorConta);
        painelInventario.atualizarDados();
        atualizarRodapeConta(identificadorConta, saldoAtual.getAsLong());
        rotuloSituacao.setText("Sessão ativa");
        gerenciadorTelas.show(painelTelas, TELA_SISTEMA);
    }

    private void sair() {
        painelSaque.encerrarSessao();
        campoContaAcesso.setText("");
        rotuloContaSaldo.setText("Conta utilizada: nenhuma  |  Saldo atual da conta: —");
        rotuloSituacao.setText("Sistema disponível");
        rotuloMensagemAcesso.setForeground(TemaInterface.SUCESSO);
        gerenciadorTelas.show(painelTelas, TELA_ACESSO);
        SwingUtilities.invokeLater(campoContaAcesso::requestFocusInWindow);
    }

    private void exibirErroAcesso(String mensagem) {
        rotuloMensagemAcesso.setForeground(TemaInterface.ERRO);
        rotuloMensagemAcesso.setText(mensagem);
        campoContaAcesso.requestFocusInWindow();
    }

    private void atualizarRodapeConta(String identificadorConta, long saldoAtualEmCentavos) {
        rotuloContaSaldo.setText(
                "Conta utilizada: " + identificadorConta +
                        "  |  Saldo atual da conta: " +
                        TemaInterface.formatarDinheiro(saldoAtualEmCentavos));
    }
}
