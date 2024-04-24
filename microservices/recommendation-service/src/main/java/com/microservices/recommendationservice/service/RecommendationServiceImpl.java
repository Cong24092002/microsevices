package com.microservices.recommendationservice.service;


import java.util.logging.Level;

import com.microservices.api.core.recommendation.Recommendation;
import com.microservices.api.core.recommendation.RecommendationService;
import com.microservices.recommendationservice.persistence.RecommendationEntity;
import com.microservices.recommendationservice.persistence.RecommendationRepository;
import com.microservices.util.http.AppException;
import com.microservices.util.http.ErrorCode;
import com.microservices.util.http.ServiceUtil;
import com.mongodb.DuplicateKeyException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    RecommendationRepository repository;

    RecommendationMapper mapper;

    ServiceUtil serviceUtil;

    @Autowired
    public RecommendationServiceImpl(RecommendationRepository repository, RecommendationMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Mono<Recommendation> createRecommendation(Recommendation body) {
        if(body.getProductId() < 1) throw new AppException(ErrorCode.INVALID_INPUT);
        RecommendationEntity entity = mapper.apiToEntity(body);
        return repository.save(entity)
                .log(LOG.getName(), Level.FINE)
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new AppException(ErrorCode.DUPLICATE_KEY_DB)
                )
                .map(mapper::entityToApi);
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {

        if (productId < 1) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }
        return repository.findByProductId(productId)
                .log(LOG.getName(), Level.FINE)
                .map(mapper::entityToApi)
                .map(this::setServiceAddress);
    }

    @Override
    public Mono<Void> deleteRecommendations(int productId) {
        if(productId < 1) throw new AppException(ErrorCode.INVALID_INPUT);
        LOG.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);
        return repository.deleteAll(repository.findByProductId(productId));
    }

    private Recommendation setServiceAddress(Recommendation e){
        e.setServiceAddress(serviceUtil.getServiceAddress());
        return e;
    }
}