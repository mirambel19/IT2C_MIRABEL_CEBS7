package com.scc.model;

// ✅ Java Record as required
public record User(
        int id,
        String username,
        String password,
        String roles,
        String status
) {}