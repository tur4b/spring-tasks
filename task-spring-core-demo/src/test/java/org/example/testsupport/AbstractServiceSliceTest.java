package org.example.testsupport;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service-layer slice base for integration tests.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ServiceLayerConfig.class })
@Transactional
public abstract class AbstractServiceSliceTest {
}

