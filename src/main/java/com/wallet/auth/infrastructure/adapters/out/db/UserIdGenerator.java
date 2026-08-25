package com.wallet.auth.infrastructure.adapters.out.db;
import org.springframework.stereotype.Component;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.FindAndModifyOptions;

@Component
public class UserIdGenerator {

    private final MongoTemplate mongoTemplate;

    public UserIdGenerator(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public long nextUserId() {
        Query query = new Query(Criteria.where("_id").is("user_sequence"));
        Update update = new Update().inc("seq", 1);
        FindAndModifyOptions options = FindAndModifyOptions.options()
                .upsert(true)
                .returnNew(true);

        UserSequence seq = mongoTemplate.findAndModify(query, update, options, UserSequence.class);
        return seq != null ? seq.getSeq() : 1L;
    }
}
