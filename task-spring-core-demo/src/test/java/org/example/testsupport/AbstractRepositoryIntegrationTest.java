package org.example.testsupport;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO / JPA slice base for integration tests.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DataConfig.class})
@Transactional
public abstract class AbstractRepositoryIntegrationTest {

//    @Autowired
//    protected EntityManager entityManager;
}