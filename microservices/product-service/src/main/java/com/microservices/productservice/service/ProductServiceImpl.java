package com.microservices.productservice.service;


import com.microservices.api.core.product.Product;
import com.microservices.api.core.product.ProductService;
import com.microservices.productservice.persistence.ProductEntity;
import com.microservices.productservice.persistence.ProductRepository;
import com.microservices.util.http.AppException;
import com.microservices.util.http.ErrorCode;
import com.microservices.util.http.ServiceUtil;
import com.mongodb.DuplicateKeyException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.util.logging.Level;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

    final ServiceUtil serviceUtil;

    final ProductRepository repository;

    final ProductMapper mapper;

    public ProductServiceImpl(ServiceUtil serviceUtil, ProductRepository repository, ProductMapper mapper) {
        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Product> createProduct(Product body) {
        if(body.getProductId() < 1) throw new AppException(ErrorCode.INVALID_INPUT);
        ProductEntity entity = mapper.toProductEntity(body);
        return repository.save(entity)
                .log(LOG.getName(), Level.FINE)
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new AppException(ErrorCode.INVALID_INPUT))
                .map(mapper::toProduct);
    }
    @Override
    public Mono<Product> getProduct(int productId) {

        if (productId < 1) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }
        return repository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.NOT_FOUND)))
                .log(LOG.getName(), Level.FINE)
                .map(mapper::toProduct)
                .map(this::setServiceAddress);
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        if(productId < 1) throw new AppException(ErrorCode.INVALID_INPUT);
        LOG.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
        return repository.findByProductId(productId)
                .log(LOG.getName(), Level.FINE)
                .map(repository::delete).flatMap(e -> e);
    }

    private Product setServiceAddress(Product e){
        e.setServiceAddress(serviceUtil.getServiceAddress());
        return e;
    }
}