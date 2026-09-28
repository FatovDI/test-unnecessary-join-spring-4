package com.example.testspring4;

import com.example.testspring4.model.Abonent;
import com.example.testspring4.model.Contract;
import com.example.testspring4.repository.ContractRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DerivedQueryJoinTest {

	@Autowired
	private ContractRepository contractRepository;

	@PersistenceContext
	private EntityManager em;

	@Test
	@Transactional
	void derivedQueryByAbonentCode_emitsLeftJoin() {
		createAbonentAndContract();

		SqlStatementCapture.clear();
		List<Contract> result = contractRepository.findByAbonentCode("ACME-001");

		assertThat(result).hasSize(1);

		String sql = SqlStatementCapture.firstContaining("from contracts");
		System.out.println("[derived findByAbonentCode] " + sql);
		assertThat(sql).as("derived query SQL").isNotNull();
		assertThat(sql).containsIgnoringCase("left join");
	}

	@Test
	@Transactional
	void explicitQueryByAbonentCode_filtersOnFkColumnWithoutJoin() {
		createAbonentAndContract();

		SqlStatementCapture.clear();
		List<Contract> result = contractRepository.findByAbonentCodeExplicit("ACME-001");

		assertThat(result).hasSize(1);

		String sql = SqlStatementCapture.firstContaining("from contracts");
		System.out.println("[explicit @Query] " + sql);
		assertThat(sql).as("explicit @Query SQL").isNotNull();
		assertThat(sql).doesNotContainIgnoringCase("join");
	}

	private void createAbonentAndContract() {
		Abonent acme = new Abonent();
		acme.setName("Acme Telecom");
		acme.setCode("ACME-001");
		em.persist(acme);

		Contract contract = new Contract();
		contract.setNumber("C-100");
		contract.setAbonent(acme);
		contractRepository.save(contract);

		em.flush();
		em.clear();
	}
}
