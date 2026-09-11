package com.fotolou.app.service.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * Ligne d'articles d'une commande boutique, exposée à plat dans {@link BoutiqueOrderDTO}
 * (sans référence circulaire vers la commande).
 */
public class OrderLineDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long productId;
    private String productTitle;
    private Long unitPrice;
    private Integer quantity;
    private String productImage;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public Long getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Long unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OrderLineDTO)) {
            return false;
        }
        OrderLineDTO that = (OrderLineDTO) o;
        return (
            Objects.equals(productId, that.productId) &&
            Objects.equals(quantity, that.quantity) &&
            Objects.equals(unitPrice, that.unitPrice)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, quantity, unitPrice);
    }

    @Override
    public String toString() {
        return (
            "OrderLineDTO{" +
            "productId=" +
            productId +
            ", productTitle='" +
            productTitle +
            '\'' +
            ", unitPrice=" +
            unitPrice +
            ", quantity=" +
            quantity +
            '}'
        );
    }
}
