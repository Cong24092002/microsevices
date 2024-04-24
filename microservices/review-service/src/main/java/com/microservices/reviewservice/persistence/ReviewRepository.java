package com.microservices.reviewservice.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ReviewRepository extends CrudRepository<ReviewEntity, String> {
    @Transactional(readOnly = true)
    List<ReviewEntity> findByProductId(int productId);
}
