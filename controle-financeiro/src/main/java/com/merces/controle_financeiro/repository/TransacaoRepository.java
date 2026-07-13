package com.merces.controle_financeiro.repository;

import com.merces.controle_financeiro.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    // Traz as transações do usuário já ordenadas pela data (da mais antiga para a mais
    // recente), independentemente da ordem em que foram cadastradas.
    List<Transacao> findByUsuarioIdOrderByDataAsc(Long usuarioId);
}