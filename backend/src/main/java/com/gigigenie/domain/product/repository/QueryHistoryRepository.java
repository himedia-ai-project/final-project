package com.gigigenie.domain.product.repository;

import com.gigigenie.domain.member.entity.Member;
import com.gigigenie.domain.product.entity.Product;
import com.gigigenie.domain.product.entity.QueryHistory;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QueryHistoryRepository extends JpaRepository<QueryHistory, Integer> {
    List<QueryHistory> findByMemberAndProduct(@NotNull Member member, @NotNull Product product);

    void deleteByMemberAndProduct(@NotNull Member member, @NotNull Product product);

    List<QueryHistory> findByMember(Member member);
}
