package com.gigigenie.domain.history.repository;

import com.gigigenie.domain.history.entity.QueryHistory;
import com.gigigenie.domain.member.entity.Member;
import com.gigigenie.domain.product.entity.Product;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QueryHistoryRepository extends JpaRepository<QueryHistory, Integer> {

    List<QueryHistory> findByMemberAndProduct(@NotNull Member member, @NotNull Product product);

    void deleteByMemberAndProduct(@NotNull Member member, @NotNull Product product);

    List<QueryHistory> findByMember(Member member);
}
