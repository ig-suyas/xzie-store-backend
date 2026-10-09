package com.xzie.user_service.service;

import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatRateLimiter {
    private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();

    public boolean allow(String user) {          // 10 messages per minute per user
        long now = System.currentTimeMillis();
        Deque<Long> q = hits.computeIfAbsent(user, k -> new ArrayDeque<>());
        synchronized (q) {
            while (!q.isEmpty() && now - q.peekFirst() > 60_000) q.pollFirst();
            if (q.size() >= 10) return false;
            q.addLast(now);
            return true;
        }
    }
}