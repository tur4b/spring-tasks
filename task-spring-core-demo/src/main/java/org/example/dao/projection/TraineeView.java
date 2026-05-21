package org.example.dao.projection;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface TraineeView {

    Long getId();
    Long getUserId();
    String getFirstName();
    String getLastName();
    String getAddress();
    LocalDate getDateOfBirth();
    boolean getIsActive();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();

}
