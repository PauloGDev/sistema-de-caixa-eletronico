package apresentacao;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

final class TemaInterface {
    static final Color FUNDO = new Color(244, 247, 251);
    static final Color SUPERFICIE = Color.WHITE;
    static final Color PRIMARIA = new Color(21, 71, 108);
    static final Color PRIMARIA_ESCURA = new Color(16, 57, 87);
    static final Color PERIGO = new Color(180, 48, 48);
    static final Color PERIGO_ESCURO = new Color(145, 36, 36);
    static final Color TEXTO = new Color(32, 39, 48);
    static final Color TEXTO_SECUNDARIO = new Color(100, 112, 125);
    static final Color BORDA = new Color(218, 225, 232);
    static final Color SUCESSO = new Color(24, 112, 74);
    static final Color FUNDO_SUCESSO = new Color(232, 247, 239);
    static final Color ERRO = new Color(175, 46, 46);
    static final Color FUNDO_ERRO = new Color(253, 237, 237);
    static final Color ALERTA = new Color(163, 102, 20);

    static final Font FONTE_TITULO = new Font("Segoe UI", Font.BOLD, 26);
    static final Font FONTE_SECAO = new Font("Segoe UI", Font.BOLD, 18);
    static final Font FONTE_CORPO = new Font("Segoe UI", Font.PLAIN, 14);
    static final Font FONTE_ROTULO = new Font("Segoe UI", Font.BOLD, 13);

    private static final Locale BRASIL = Locale.forLanguageTag("pt-BR");

    private TemaInterface() {
    }

    static Border bordaCartao(int padding) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDA),
                BorderFactory.createEmptyBorder(padding, padding, padding, padding));
    }

    static JButton criarBotaoPrincipal(String texto) {
        return criarBotaoPreenchido(texto, PRIMARIA, PRIMARIA_ESCURA);
    }

    static JButton criarBotaoPerigo(String texto) {
        return criarBotaoPreenchido(texto, PERIGO, PERIGO_ESCURO);
    }

    static JButton criarBotaoSecundario(String texto) {
        JButton botao = new JButton(texto);
        botao.setUI(new BasicButtonUI());
        botao.setFont(FONTE_ROTULO);
        botao.setForeground(PRIMARIA);
        botao.setBackground(SUPERFICIE);
        botao.setFocusPainted(false);
        botao.setOpaque(true);
        botao.setContentAreaFilled(true);
        botao.setBorderPainted(true);
        botao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARIA),
                BorderFactory.createEmptyBorder(10, 21, 10, 21)));
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evento) {
                if (botao.isEnabled()) {
                    botao.setBackground(new Color(232, 240, 246));
                }
            }

            @Override
            public void mouseExited(MouseEvent evento) {
                if (botao.isEnabled()) {
                    botao.setBackground(SUPERFICIE);
                }
            }
        });

        return botao;
    }

    private static JButton criarBotaoPreenchido(
            String texto,
            Color corNormal,
            Color corAoPassarMouse) {
        JButton botao = new JButton(texto);
        botao.setUI(new BasicButtonUI());
        botao.setFont(FONTE_ROTULO);
        botao.setForeground(Color.WHITE);
        botao.setBackground(corNormal);
        botao.setFocusPainted(false);
        botao.setOpaque(true);
        botao.setContentAreaFilled(true);
        botao.setBorderPainted(false);
        botao.setBorder(BorderFactory.createEmptyBorder(11, 22, 11, 22));
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evento) {
                if (botao.isEnabled()) {
                    botao.setBackground(corAoPassarMouse);
                }
            }

            @Override
            public void mouseExited(MouseEvent evento) {
                if (botao.isEnabled()) {
                    botao.setBackground(corNormal);
                }
            }
        });

        return botao;
    }

    static void configurarCampo(JComponent componente) {
        componente.setFont(FONTE_CORPO);
        componente.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDA),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));
    }

    static String formatarDinheiro(long valorEmCentavos) {
        return NumberFormat.getCurrencyInstance(BRASIL)
                .format(BigDecimal.valueOf(valorEmCentavos, 2));
    }
}
