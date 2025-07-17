package test.utpl.msvc_sales.services;

import java.util.List;
import java.util.Optional;

import test.utpl.msvc_sales.entities.Items;

public interface ItemsService {

    List<Items> getAllItems();

    Optional<Items> getItemById(String id);

}
