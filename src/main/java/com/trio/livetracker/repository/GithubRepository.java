package com.trio.livetracker.repository;

import com.trio.livetracker.document.DocRepo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GithubRepository extends ReactiveMongoRepository<DocRepo, String> {

}
