package com.trio.livetracker.repository;

import com.trio.livetracker.document.DocRepo;
import com.trio.livetracker.document.RepoCountAnalytic;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.Date;

@Repository
public interface AnalyticRepository extends ReactiveMongoRepository<DocRepo, String> {

    @Aggregation(pipeline = {
            "{$match: { $and: [ {codeUpdates: {$elemMatch: {keyWord: ?0}}}, {codeUpdates: {$elemMatch: {timeCreated: {$gte: ?1, $lt: ?2}}}} ]}}",
            "{ $unwind: { path: '$codeUpdates' } }",
            "{$group: { _id: '$_id', count: {$sum: {\n" +
                    "                        $cond: [{$eq: [?0, '$codeUpdates.keyWord']}, 1, 0]\n" +
                    "                    }}}}",
            "{$sort:{count:-1}}"
    })
    Flux<RepoCountAnalytic> findTop5ByCodeUpdatesContainsKeyword(String keyword, Date fromDate, Date toDate);

    @Aggregation(pipeline = {
            "{$match: {codeUpdates: {$elemMatch: {keyWord: ?0}}}}",
            "{$unwind: { path: '$languages' } }",
            "{$group:\n" +
                    "            {\n" +
                    "                _id: '$languages',\n" +
                    "                count: {$sum: 1}\n" +
                    "            },\n" +
                    "    }",
            "{$sort:{count:-1}}"
    })
    Flux<RepoCountAnalytic> findByKeyword(String keyword);
}
