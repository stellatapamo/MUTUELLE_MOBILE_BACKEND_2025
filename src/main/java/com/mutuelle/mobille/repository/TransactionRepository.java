package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Récupérer toutes les transactions de type ... (ex:EPARGNE)
    List<Transaction> findByTransactionType(TransactionType transactionType);

    //Récupérer toutes les transactions de type EPARGNE et d'une transactionDirection precise
    List<Transaction> findByTransactionTypeAndTransactionDirection(TransactionType type, TransactionDirection direction);

    // Exemple : filtrer par membre
    List<Transaction> findByTransactionTypeAndAccountMemberId(TransactionType transactionType, Long memberId);

    //filtrer en fonction de TransactionDirection et de membre
    List<Transaction> findByAccountMemberIdAndTransactionTypeAndTransactionDirection(
            Long memberId, TransactionType type, TransactionDirection direction);

}
