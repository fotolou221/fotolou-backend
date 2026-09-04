package com.fotolou.app.service.mapper;

import com.fotolou.app.domain.Product;
import com.fotolou.app.domain.ProductImage;
import com.fotolou.app.service.dto.ProductDTO;
import com.fotolou.app.service.dto.ProductImageDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ProductImage} and its DTO {@link ProductImageDTO}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductImageMapper extends EntityMapper<ProductImageDTO, ProductImage> {
    @Mapping(target = "product", source = "product", qualifiedByName = "productTitle")
    ProductImageDTO toDto(ProductImage s);

    @Named("productTitle")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    ProductDTO toDtoProductTitle(Product product);
}
