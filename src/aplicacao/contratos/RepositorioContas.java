package aplicacao.contratos;

import dominio.Conta;

import java.util.Optional;

public interface RepositorioContas {
    Optional<Conta> buscarPorIdentificador(String identificadorConta);

    void adicionar(Conta conta);
}
