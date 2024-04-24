package com.microservices.productservice.service;

import com.microservices.api.core.product.Product;
import com.microservices.productservice.persistence.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mappings({
            @Mapping(target = "id", ignore = true), @Mapping(target = "version", ignore = true)
    })
    ProductEntity toProductEntity(Product product);

    @Mapping(target = "serviceAddress", ignore = true)
    Product toProduct(ProductEntity entity);
}
