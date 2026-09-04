package com.fotolou.app.service.mapper;

import com.fotolou.app.domain.BoutiqueOrder;
import com.fotolou.app.domain.OrderItem;
import com.fotolou.app.domain.Product;
import com.fotolou.app.service.dto.BoutiqueOrderDTO;
import com.fotolou.app.service.dto.OrderItemDTO;
import com.fotolou.app.service.dto.ProductDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link OrderItem} and its DTO {@link OrderItemDTO}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderItemMapper extends EntityMapper<OrderItemDTO, OrderItem> {
    @Mapping(target = "product", source = "product", qualifiedByName = "productId")
    @Mapping(target = "order", source = "order", qualifiedByName = "boutiqueOrderOrderNumber")
    OrderItemDTO toDto(OrderItem s);

    @Named("productId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ProductDTO toDtoProductId(Product product);

    @Named("boutiqueOrderOrderNumber")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "orderNumber", source = "orderNumber")
    BoutiqueOrderDTO toDtoBoutiqueOrderOrderNumber(BoutiqueOrder boutiqueOrder);
}
