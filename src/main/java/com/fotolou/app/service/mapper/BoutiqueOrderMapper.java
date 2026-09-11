package com.fotolou.app.service.mapper;

import com.fotolou.app.domain.BoutiqueOrder;
import com.fotolou.app.domain.OrderItem;
import com.fotolou.app.domain.User;
import com.fotolou.app.service.dto.BoutiqueOrderDTO;
import com.fotolou.app.service.dto.OrderLineDTO;
import com.fotolou.app.service.dto.UserDTO;
import java.util.List;
import java.util.Set;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link BoutiqueOrder} and its DTO {@link BoutiqueOrderDTO}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BoutiqueOrderMapper extends EntityMapper<BoutiqueOrderDTO, BoutiqueOrder> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userId")
    @Mapping(target = "items", source = "itemses", qualifiedByName = "orderLines")
    @Mapping(target = "whatsAppUrl", ignore = true)
    BoutiqueOrderDTO toDto(BoutiqueOrder s);

    @Named("userId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    UserDTO toDtoUserId(User user);

    @Named("orderLines")
    default List<OrderLineDTO> toOrderLines(Set<OrderItem> items) {
        if (items == null) {
            return List.of();
        }
        return items
            .stream()
            .sorted(java.util.Comparator.comparing(i -> i.getId() == null ? Long.MAX_VALUE : i.getId()))
            .map(BoutiqueOrderMapper::toOrderLine)
            .toList();
    }

    static OrderLineDTO toOrderLine(OrderItem item) {
        OrderLineDTO line = new OrderLineDTO();
        line.setProductId(item.getProduct() != null ? item.getProduct().getId() : null);
        line.setProductTitle(item.getProductTitle());
        line.setUnitPrice(item.getUnitPrice());
        line.setQuantity(item.getQuantity());
        return line;
    }
}
