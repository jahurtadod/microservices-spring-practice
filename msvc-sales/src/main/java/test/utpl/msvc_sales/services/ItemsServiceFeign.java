package test.utpl.msvc_sales.services;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import feign.FeignException;
import test.utpl.msvc_sales.client.ProductFeingClient;
import test.utpl.msvc_sales.entities.Items;
import test.utpl.msvc_sales.entities.ProductDTO;

@Service
public class ItemsServiceFeign implements ItemsService {

    @Autowired
    private ProductFeingClient productFeignClient;

    @Override
    public List<Items> getAllItems() {
        return productFeignClient.getAllProducts()
                .stream()
                .map(product -> new Items(product, new Random().nextInt(10) + 1))
                .collect(Collectors.toList());

        // return productFeignClient.getAllProducts().stream().map(product -> {
        // Random random = new Random();
        // Integer quantity = random.nextInt(10) + 1;
        // return new Items(product, quantity);
        // }).collect(Collectors.toList());
    }

    @Override
    public Optional<Items> getItemById(String id) {
        try {
            ProductDTO product = productFeignClient.getProductById(id);
            return Optional.of(new Items(product, new Random().nextInt(10) + 1));
        } catch (FeignException e) {
            return Optional.empty();
        }

        // ProductDTO product = productFeignClient.getProductById(id);
        // if (product != null) {
        // return Optional.of(new Items(product, new Random().nextInt(10) + 1));
        // } else {
        // return Optional.empty();
        // }
    }

}
