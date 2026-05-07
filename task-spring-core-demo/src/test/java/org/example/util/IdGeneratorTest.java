package org.example.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("IdGenerator Unit Tests")
class IdGeneratorTest {

    @Test
    @DisplayName("getNextId - starts at 1 for a new entity type")
    void getNextId_StartsAtOne() {
        IdGenerator generator = new IdGenerator();

        Long id = generator.getNextId("TypeStartsAtOne");

        assertThat(id).isEqualTo(1L);
    }

    @Test
    @DisplayName("getNextId - increments on each call for same entity type")
    void getNextId_Increments() {
        IdGenerator generator = new IdGenerator();

        Long id1 = generator.getNextId("TypeIncrements");
        Long id2 = generator.getNextId("TypeIncrements");
        Long id3 = generator.getNextId("TypeIncrements");

        assertThat(id1).isEqualTo(1L);
        assertThat(id2).isEqualTo(2L);
        assertThat(id3).isEqualTo(3L);
    }

    @Test
    @DisplayName("getNextId - different entity types maintain independent counters")
    void getNextId_IndependentCountersPerType() {
        IdGenerator generator = new IdGenerator();

        Long typeAId1 = generator.getNextId("TypeA_IndependentA");
        Long typeAId2 = generator.getNextId("TypeA_IndependentA");
        Long typeBId1 = generator.getNextId("TypeB_IndependentB");

        assertThat(typeAId1).isEqualTo(1L);
        assertThat(typeAId2).isEqualTo(2L);
        assertThat(typeBId1).isEqualTo(1L);
    }

    @Test
    @DisplayName("getNextId - returns a non-null positive value")
    void getNextId_ReturnsPositiveValue() {
        IdGenerator generator = new IdGenerator();

        Long id = generator.getNextId("TypePositive");

        assertThat(id).isNotNull().isPositive();
    }

    @Test
    @DisplayName("getNextId - same entity type string used across instances shares the counter (static map)")
    void getNextId_StaticMapSharedAcrossInstances() {
        IdGenerator gen1 = new IdGenerator();
        IdGenerator gen2 = new IdGenerator();

        long first  = gen1.getNextId("SharedType_StaticTest");
        long second = gen2.getNextId("SharedType_StaticTest"); // same static map entry

        assertThat(second).isEqualTo(first + 1);
    }
}

