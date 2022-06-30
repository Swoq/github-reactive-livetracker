package com.trio.livetracker.repository;

import com.trio.livetracker.document.CodeUpdate;
import com.trio.livetracker.document.DocRepo;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface GithubRepository extends ReactiveMongoRepository<DocRepo, String> {
    @Query("{'codeUpdates._id' : ?0 }")
    Mono<DocRepo> findByCodeUpdateId(String codeUpdateId);
}
