package com.merces.controle_financeiro.repository;

import com.merces.controle_financeiro.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    // Filtra as transações, trazendo apenas as que pertencem ao usuário informado
    List<Transacao> findByUsuarioId(Long usuarioId);
}