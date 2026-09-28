# Derived query JOIN reproducer (Spring Data JPA)

Minimal reproducer for the Spring Data JPA derived-query regression where a query method that
navigates a `@ManyToOne` association (e.g. `findByAbonentCode`) emits a redundant `LEFT JOIN`
instead of filtering directly on the foreign-key column.

## Model

```java
@Entity
@Table(name = "abonents")
class Abonent {
    @Id @GeneratedValue Long id;
    @Column(unique = true) String code;
}

@Entity
@Table(name = "contracts")
class Contract {
    @Id @GeneratedValue Long id;
    @Column String number;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "abonent", referencedColumnName = "code")
    Abonent abonent;
}
```

`ContractRepository` has two lookup methods:

- `findByAbonentCode(String)` — derived query (the subject of the regression).
- `findByAbonentCodeExplicit(String)` — the same predicate written explicitly via `@Query`.

## Branches

| Branch         | Spring Boot | Spring Data JPA | `findByAbonentCode` SQL                                                |
|----------------|-------------|-----------------|------------------------------------------------------------------------|
| `main`         | 4.1.0       | 4.x             | `left join abonents a on a.code = c.abonent where a.code = ?`          |
| `old-behavior` | 3.1.2       | 3.1.x           | `where c.abonent = ?` (no join)                                        |

The explicit `@Query` variant renders join-free on **both** branches, confirming the join is
introduced by the derived-query engine, not by Hibernate.

## Run

```bash
./gradlew test
```

The test captures the generated SQL via a Hibernate `StatementInspector`, prints it, and asserts
on it. Expected output:

```
[derived findByAbonentCode] select ... from contracts c1_0 left join abonents a1_0 on a1_0.code=c1_0.abonent where a1_0.code=?
[explicit @Query]        select ... from contracts c1_0 where c1_0.abonent=?
```
