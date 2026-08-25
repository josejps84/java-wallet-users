package com.wallet.auth.infrastructure.adapters.out.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "counters")
@Data
public class UserSequence {
    @Id
    private String id;
    private long seq;
}
