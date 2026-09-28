package com.example.testspring4.repository;

import com.example.testspring4.model.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    // Derived query: the path "abonent" -> Abonent.code is navigated through the @ManyToOne.
    // Spring Data JPA 4.x emits an explicit LEFT JOIN for this predicate.
    List<Contract> findByAbonentCode(String abonentCode);

    // Same predicate written explicitly: Hibernate collapses it onto the FK column, no JOIN.
    @Query("select c from Contract c where c.abonent.code = :code")
    List<Contract> findByAbonentCodeExplicit(@Param("code") String code);
}
