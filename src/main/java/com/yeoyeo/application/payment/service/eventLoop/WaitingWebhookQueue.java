package com.yeoyeo.application.payment.service.eventLoop;

import com.yeoyeo.application.payment.dto.WaitingWebhookDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Queue;

@Slf4j
@RequiredArgsConstructor
@Component
public class WaitingWebhookQueue {

    private final Queue<WaitingWebhookDto> waitingWebhookDtoQueue = new LinkedList<>();

    public boolean add(WaitingWebhookDto waitingWebhookDto) {
        return this.waitingWebhookDtoQueue.offer(waitingWebhookDto);
    }

    public WaitingWebhookDto getFirstWebhook() {
        return this.waitingWebhookDtoQueue.peek();
    }

    public WaitingWebhookDto popFirstWebhook() {
        return this.waitingWebhookDtoQueue.poll();
    }

    public int countWaitingWebhook() {
        return this.waitingWebhookDtoQueue.size();
    }

}
