package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.models.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    // Méthodes simples (optionnelles, utiles pour d'autres cas)
    List<Transaction> findByTransactionType(TransactionType type);

    List<Transaction> findByTransactionDirection(TransactionDirection direction);

    List<Transaction> findByTransactionTypeAndTransactionDirection(TransactionType type, TransactionDirection direction);

    // Méthode dynamique avec filtres (utilisée dans le service)
    default Page<Transaction> findFiltered(
            TransactionType type,
            TransactionDirection direction,
            Long sessionId,
            Long accountMemberId,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {

        Specification<Transaction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (type != null) {
                predicates.add(cb.equal(root.get("transactionType"), type));
            }
            if (direction != null) {
                predicates.add(cb.equal(root.get("transactionDirection"), direction));
            }
            if (sessionId != null) {
                predicates.add(cb.equal(root.get("session").get("id"), sessionId));
            }
            if (accountMemberId != null) {
                predicates.add(cb.equal(root.get("accountMember").get("id"), accountMemberId));
            }
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return findAll(spec, pageable);
    }


    /**
     * Récupère toutes les transactions de type ASSISTANCE pour une session donnée
     */
    List<Transaction> findBySessionAndTransactionType(
            Session session,
            TransactionType type
    );

    /**
     * Version avec ID de session (plus légère si on n'a pas l'entité Session chargée)
     */
    @Query("SELECT t FROM Transaction t " +
            "WHERE t.session.id = :sessionId " +
            "AND t.transactionType = :type")
    List<Transaction> findBySessionIdAndTransactionType(
            @Param("sessionId") Long sessionId,
            @Param("type") TransactionType type
    );

    /**
     * Somme des montants des assistances pour une session
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) " +
            "FROM Transaction t " +
            "WHERE t.session.id = :sessionId " +
            "AND t.transactionType = com.mutuelle.mobille.enums.TransactionType.ASSISTANCE")
    BigDecimal sumAssistanceAmountBySessionId(@Param("sessionId") Long sessionId);

    /**
     * Somme des montants des agapes pour une session
     * (normalement il n'y en a qu'une par session, mais on somme au cas où)
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) " +
            "FROM Transaction t " +
            "WHERE t.session.id = :sessionId " +
            "AND t.transactionType = com.mutuelle.mobille.enums.TransactionType.AGAPE")
    BigDecimal sumAgapeAmountBySessionId(@Param("sessionId") Long sessionId);

    @Query("""
    SELECT COUNT(t)
    FROM Transaction t
    WHERE t.session.id = :sessionId """)
    Long countBySessionId(@Param("sessionId") Long sessionId);

    /**
     * Somme totale des dépenses (assistances + agapes) pour une session
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) " +
            "FROM Transaction t " +
            "WHERE t.session.id = :sessionId " +
            "AND t.transactionType IN (" +
            "   com.mutuelle.mobille.enums.TransactionType.ASSISTANCE, " +
            "   com.mutuelle.mobille.enums.TransactionType.AGAPE" +
            ")")
    BigDecimal sumTotalExpensesBySessionId(@Param("sessionId") Long sessionId);

    /**
     * Toutes les transactions de cotisation solidarité pour une session
     * (utile pour vérification ou rapport)
     */
    @Query("SELECT t FROM Transaction t " +
            "WHERE t.session.id = :sessionId " +
            "AND t.transactionType = com.mutuelle.mobille.enums.TransactionType.SOLIDARITE")
    List<Transaction> findSolidarityContributionsBySession(@Param("sessionId") Long sessionId);

    /**
     * Nombre de membres ayant cotisé à la solidarité pour une session
     * (si tu crées une transaction SOLIDARITE par membre)
     */
    @Query("SELECT COUNT(DISTINCT t.accountMember) " +
            "FROM Transaction t " +
            "WHERE t.session.id = :sessionId " +
            "AND t.transactionType = com.mutuelle.mobille.enums.TransactionType.SOLIDARITE")
    Long countMembersWithSolidarityContribution(@Param("sessionId") Long sessionId);

    // ───────────────────────────────────────────────
    // Méthodes temporelles utiles pour rapports globaux
    // ───────────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(t.amount), 0) " +
            "FROM Transaction t " +
            "WHERE t.createdAt >= :from AND t.createdAt <= :to " +
            "AND t.transactionType = :type")
    BigDecimal sumAmountByTypeAndPeriod(
            @Param("type") TransactionType type,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.accountMember.id = :accountMemberId")
    Long countByAccountMemberId(@Param("accountMemberId") Long accountMemberId);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.accountMember.member.id = :memberId")
    Long countByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT COUNT(t) FROM Transaction t")
    Long countTotalTransactions();

    @Query("""
    SELECT COALESCE(SUM(t.amount), 0)
    FROM Transaction t
    WHERE t.transactionType = :type""")
    BigDecimal sumAmountByType(@Param("type") TransactionType type);

}
