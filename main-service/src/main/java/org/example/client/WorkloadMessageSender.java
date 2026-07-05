package org.example.client;

import org.example.common.dto.WorkloadEventRequest;

public interface WorkloadMessageSender {

    void send(WorkloadEventRequest request);
}
