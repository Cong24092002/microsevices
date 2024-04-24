package com.microservices.reviewservice.service;

import java.util.List;
import java.util.logging.Level;

import com.microservices.api.core.review.Review;
import com.microservices.api.core.review.ReviewService;
import com.microservices.api.exceptions.InvalidInputException;
import com.microservices.reviewservice.persistence.ReviewEntity;
import com.microservices.reviewservice.persistence.ReviewRepository;
import com.microservices.util.http.AppException;
import com.microservices.util.http.ErrorCode;
import com.microservices.util.http.ServiceUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Service
public class ReviewServiceImpl implements ReviewService {
    static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);

    final ReviewRepository repository;

    final ReviewMapper mapper;

    final ServiceUtil serviceUtil;
    final Scheduler jdbcScheduler;

    @Override
    public Mono<Review> createReview(Review body) {
        if(body.getProductId() < 1) throw new AppException(ErrorCode.INVALID_INPUT);
        return Mono.fromCallable(() -> internalCreateReview(body))
                    .subscribeOn(jdbcScheduler);
    }
    private Review internalCreateReview(Review body) {
        try {
            ReviewEntity entity = mapper.apiToEntity(body);
            ReviewEntity newEntity = repository.save(entity);

            LOG.debug("createReview: created a review entity: {}/{}", body.getProductId(), body.getReviewId());
            return mapper.entityToApi(newEntity);

        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Review Id:" + body.getReviewId());
        }
    }

    @Override
    public Flux<Review> getReviews(int productId) {

        if (productId < 1) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }
        List<ReviewEntity> entityList = repository.findByProductId(productId);
        List<Review> list = mapper.entityListToApiList(entityList);
        list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

        LOG.debug("getReviews: response size: {}", list.size());

        return Mono.fromCallable(() -> internalGetReviews(productId))
                .flatMapMany(Flux::fromIterable)
                .log(LOG.getName(), Level.FINE)
                .subscribeOn(jdbcScheduler);
    }

    @Override
    public Mono<Void> deleteReviews(int productId) {
        if(productId < 1) throw new AppException(ErrorCode.INVALID_INPUT);
        return Mono.fromRunnable(() -> internalDeleteReview(productId))
                .subscribeOn(jdbcScheduler).then();
    }
    private void internalDeleteReview(int productId){
        repository.deleteAll(repository.findByProductId(productId));
    }

    private List<Review> internalGetReviews(int productId) {

        List<ReviewEntity> entityList = repository.findByProductId(productId);
        List<Review> list = mapper.entityListToApiList(entityList);
        list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

        LOG.debug("Response size: {}", list.size());

        return list;
    }

}