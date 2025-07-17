package test.utpl.msvc_sales.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import test.utpl.msvc_sales.entities.ProductDTO;

@FeignClient(name = "msvc-products")
public interface ProductFeingClient {

    @GetMapping("api/products")
    List<ProductDTO> getAllProducts();

    @GetMapping("api/products/{id}")
    ProductDTO getProductById(@PathVariable String id);

}
