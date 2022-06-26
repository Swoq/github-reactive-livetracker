package com.trio.livetracker.repository;

import com.trio.livetracker.document.DocRepo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

@Repository
public interface GithubRepository extends ReactiveMongoRepository<DocRepo,String> {

}
