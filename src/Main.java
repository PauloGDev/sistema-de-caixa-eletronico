import configuracao.ConfiguracaoAplicacao;
import configuracao.DependenciasAplicacao;
import apresentacao.JanelaPrincipal;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {
    private Main() {
    }

    public static void main(String[] argumentos) {
        configurarAparenciaSistema();

        SwingUtilities.invokeLater(() -> {
            DependenciasAplicacao dependencias = ConfiguracaoAplicacao.criar();
            JanelaPrincipal janela = new JanelaPrincipal(dependencias);
            janela.setVisible(true);
        });
    }

    private static void configurarAparenciaSistema() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception excecaoIgnorada) {

        }
    }
}
