package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.models.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import jakarta.persistence.criteria.Predicate;
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
}
