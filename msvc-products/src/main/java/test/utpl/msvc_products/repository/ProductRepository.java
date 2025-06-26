package test.utpl.msvc_products.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import test.utpl.msvc_products.models.entities.Product;

public interface ProductRepository extends MongoRepository<Product, String> {

}
