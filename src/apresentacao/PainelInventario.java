package apresentacao;

import aplicacao.ServicoInventario;
import dominio.InventarioDinheiro;
import dominio.Denominacao;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

final class PainelInventario extends JPanel {
    private final ServicoInventario servicoInventario;
    private final InventarioDinheiro inventario;
    private final ModeloTabelaInventario modeloTabela = new ModeloTabelaInventario();

    private final JLabel rotuloTotal = new JLabel();
    private final JLabel rotuloStatusOperacao = new JLabel(" ");
    private final JSpinner seletorDenominacao = new JSpinner(
            new SpinnerNumberModel(20.00, 0.01, 100_000.00, 1.00));
    private final JSpinner seletorQuantidade = new JSpinner(
            new SpinnerNumberModel(1, 1, 10_000, 1));

    PainelInventario(ServicoInventario servicoInventario, InventarioDinheiro inventario) {
        this.servicoInventario = servicoInventario;
        this.inventario = inventario;

        setLayout(new BorderLayout(18, 18));
        setBackground(TemaInterface.FUNDO);
        setBorder(BorderFactory.createEmptyBorder(14, 10, 14, 10));

        add(criarResumo(), BorderLayout.NORTH);
        add(criarCartaoTabela(), BorderLayout.CENTER);
        add(criarCartaoOperacao(), BorderLayout.EAST);

        atualizarDados();
    }

    void atualizarDados() {
        modeloTabela.atualizar(inventario.obterCopiaEstoque());
        rotuloTotal.setText("Total disponível no caixa: " +
                TemaInterface.formatarDinheiro(inventario.getValorTotalEmCentavos()));
    }

    private JPanel criarResumo() {
        JPanel resumo = new JPanel(new BorderLayout());
        resumo.setOpaque(false);

        JLabel titulo = new JLabel("Inventário de cédulas e moedas");
        titulo.setFont(TemaInterface.FONTE_SECAO);
        titulo.setForeground(TemaInterface.TEXTO);

        rotuloTotal.setFont(TemaInterface.FONTE_ROTULO);
        rotuloTotal.setForeground(TemaInterface.PRIMARIA);

        resumo.add(titulo, BorderLayout.WEST);
        resumo.add(rotuloTotal, BorderLayout.EAST);
        return resumo;
    }

    private JPanel criarCartaoTabela() {
        JTable tabela = new JTable(modeloTabela);
        tabela.setFont(TemaInterface.FONTE_CORPO);
        tabela.setRowHeight(34);
        tabela.setFillsViewportHeight(true);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setGridColor(TemaInterface.BORDA);
        tabela.setSelectionBackground(new java.awt.Color(220, 235, 245));
        tabela.setSelectionForeground(TemaInterface.TEXTO);
        tabela.getTableHeader().setFont(TemaInterface.FONTE_ROTULO);
        tabela.getTableHeader().setBackground(new java.awt.Color(235, 240, 245));
        tabela.getTableHeader().setForeground(TemaInterface.TEXTO);

        DefaultTableCellRenderer renderizadorCentralizado = new DefaultTableCellRenderer();
        renderizadorCentralizado.setHorizontalAlignment(SwingConstants.CENTER);
        tabela.getColumnModel().getColumn(1).setCellRenderer(renderizadorCentralizado);

        JScrollPane painelRolagem = new JScrollPane(tabela);
        painelRolagem.setBorder(BorderFactory.createEmptyBorder());

        JPanel cartao = new JPanel(new BorderLayout());
        cartao.setBackground(TemaInterface.SUPERFICIE);
        cartao.setBorder(TemaInterface.bordaCartao(12));
        cartao.add(painelRolagem, BorderLayout.CENTER);
        return cartao;
    }

    private JPanel criarCartaoOperacao() {
        JPanel cartao = new JPanel(new GridBagLayout());
        cartao.setBackground(TemaInterface.SUPERFICIE);
        cartao.setBorder(TemaInterface.bordaCartao(18));
        cartao.setPreferredSize(new Dimension(285, 0));

        GridBagConstraints restricoes = new GridBagConstraints();
        restricoes.gridx = 0;
        restricoes.gridwidth = 2;
        restricoes.fill = GridBagConstraints.HORIZONTAL;
        restricoes.weightx = 1;
        restricoes.insets = new Insets(5, 5, 5, 5);

        JLabel titulo = new JLabel("Movimentar estoque");
        titulo.setFont(TemaInterface.FONTE_SECAO);
        titulo.setForeground(TemaInterface.TEXTO);
        restricoes.gridy = 0;
        cartao.add(titulo, restricoes);

        JLabel rotuloDenominacao = criarRotulo("Denominação (R$)");
        restricoes.gridy = 1;
        restricoes.insets = new Insets(18, 5, 4, 5);
        cartao.add(rotuloDenominacao, restricoes);

        seletorDenominacao.setEditor(
                new JSpinner.NumberEditor(seletorDenominacao, "#,##0.00"));
        TemaInterface.configurarCampo(seletorDenominacao);
        restricoes.gridy = 2;
        restricoes.insets = new Insets(4, 5, 8, 5);
        cartao.add(seletorDenominacao, restricoes);

        JLabel rotuloQuantidade = criarRotulo("Quantidade");
        restricoes.gridy = 3;
        cartao.add(rotuloQuantidade, restricoes);

        TemaInterface.configurarCampo(seletorQuantidade);
        restricoes.gridy = 4;
        cartao.add(seletorQuantidade, restricoes);

        JButton botaoCarregar = TemaInterface.criarBotaoPrincipal("Carregar");
        botaoCarregar.addActionListener(evento -> movimentarInventario(true));
        restricoes.gridy = 5;
        restricoes.gridwidth = 1;
        restricoes.insets = new Insets(18, 5, 5, 5);
        cartao.add(botaoCarregar, restricoes);

        JButton botaoDescarregar = TemaInterface.criarBotaoPerigo("Descarregar");
        botaoDescarregar.addActionListener(evento -> movimentarInventario(false));
        restricoes.gridx = 1;
        cartao.add(botaoDescarregar, restricoes);

        rotuloStatusOperacao.setFont(TemaInterface.FONTE_CORPO);
        rotuloStatusOperacao.setVerticalAlignment(SwingConstants.TOP);
        restricoes.gridx = 0;
        restricoes.gridy = 6;
        restricoes.gridwidth = 2;
        restricoes.weighty = 1;
        restricoes.anchor = GridBagConstraints.NORTH;
        restricoes.insets = new Insets(16, 5, 5, 5);
        cartao.add(rotuloStatusOperacao, restricoes);

        return cartao;
    }

    private JLabel criarRotulo(String texto) {
        JLabel rotulo = new JLabel(texto);
        rotulo.setFont(TemaInterface.FONTE_ROTULO);
        rotulo.setForeground(TemaInterface.TEXTO);
        return rotulo;
    }

    private void movimentarInventario(boolean carregar) {
        long denominacaoEmCentavos = Math.round(
                ((Number) seletorDenominacao.getValue()).doubleValue() * 100);
        int quantidade = ((Number) seletorQuantidade.getValue()).intValue();

        try {
            boolean eventoRegistrado;
            String mensagem;
            if (carregar) {
                eventoRegistrado = servicoInventario.carregar(denominacaoEmCentavos, quantidade);
                mensagem = "Estoque carregado com sucesso.";
            } else {
                eventoRegistrado = servicoInventario.descarregar(denominacaoEmCentavos, quantidade);
                mensagem = "Estoque descarregado com sucesso.";
            }
            if (!eventoRegistrado) {
                mensagem += " Não foi possível registrar o evento no arquivo.";
            }
            exibirSucessoOperacao(mensagem);
            atualizarDados();
        } catch (RuntimeException excecao) {
            rotuloStatusOperacao.setForeground(TemaInterface.ERRO);
            rotuloStatusOperacao.setText("<html>" + excecao.getMessage() + "</html>");
        }
    }

    private void exibirSucessoOperacao(String mensagem) {
        rotuloStatusOperacao.setForeground(TemaInterface.SUCESSO);
        rotuloStatusOperacao.setText("<html>" + mensagem + "</html>");
    }

    private static final class ModeloTabelaInventario extends AbstractTableModel {
        private final String[] colunas = {"Denominação", "Quantidade", "Subtotal"};
        private List<Map.Entry<Denominacao, Integer>> linhas = List.of();

        void atualizar(Map<Denominacao, Integer> estoque) {
            linhas = new ArrayList<>(estoque.entrySet());
            linhas.sort(Map.Entry.<Denominacao, Integer>comparingByKey(
                    Comparator.reverseOrder()));
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return linhas.size();
        }

        @Override
        public int getColumnCount() {
            return colunas.length;
        }

        @Override
        public String getColumnName(int coluna) {
            return colunas[coluna];
        }

        @Override
        public Object getValueAt(int indiceLinha, int indiceColuna) {
            Map.Entry<Denominacao, Integer> linha = linhas.get(indiceLinha);
            return switch (indiceColuna) {
                case 0 -> linha.getKey().toString();
                case 1 -> linha.getValue();
                case 2 -> TemaInterface.formatarDinheiro(
                        linha.getKey().valorEmCentavos() * linha.getValue());
                default -> "";
            };
        }

        @Override
        public Class<?> getColumnClass(int indiceColuna) {
            return indiceColuna == 1 ? Integer.class : String.class;
        }
    }
}
