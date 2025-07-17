package test.utpl.msvc_sales.entities;

public class Items {

    private ProductDTO product;
    private int quantity;

    public Items(ProductDTO product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public ProductDTO getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public Double getTotal() {
        return product.getPrice() * quantity;
    }

    public Double getIva() {
        return (product.getPrice() * quantity) * 0.15;
    }
}
