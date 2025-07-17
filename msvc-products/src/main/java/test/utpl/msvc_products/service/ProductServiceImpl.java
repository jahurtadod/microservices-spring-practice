package test.utpl.msvc_products.service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;

import test.utpl.msvc_products.models.entities.Product;
import test.utpl.msvc_products.repository.ProductRepository;

@Service
public class ProductServiceImpl implements ProductService {

    private ProductRepository productRepository;
    private Environment environment;

    public ProductServiceImpl(ProductRepository productRepository, Environment environment) {
        this.productRepository = productRepository;
        this.environment = environment;
    }

    @Override
    public String importProductsFromFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return "Archivo no valido";
        }
        try {
            InputStream inputStream = file.getInputStream();
            if (fileName.endsWith(".csv")) {
                return importProductsFromCsv(inputStream);
            }
            if (fileName.endsWith(".xlsx")) {
                return importProductsFromExcel(inputStream);
            }
            return "Formato de archivo no soportado. Use CSV o Excel.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al procesar el archivo: " + e.getMessage();
        }
    }

    private String importProductsFromCsv(InputStream inputStream) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            String[] headers = reader.readNext();
            if (headers == null) {
                return "El archivo esta vacío o no tiene encabezados";
            }

            int nameIndex = -1;
            int descriptionIndex = -1;
            int priceIndex = -1;
            int stockIndex = -1;

            System.err.println("Headers: " + String.join(", ", headers));

            for (int i = 0; i < headers.length; i++) {
                switch (headers[i].trim().toLowerCase()) {
                    case "name":
                        nameIndex = i;
                        break;
                    case "description":
                        descriptionIndex = i;
                        break;
                    case "price ":
                        priceIndex = i;
                        break;
                    case "stock":
                        stockIndex = i;
                        break;
                }
            }

            System.err.println("Indices - Name: " + nameIndex + ", Description: " + descriptionIndex +
                    ", Price: " + priceIndex + ", Stock: " + stockIndex);

            if (nameIndex == -1 || priceIndex == -1 || stockIndex == -1 || descriptionIndex == -1) {
                return "El archivo CSV debe contener las columnas: name, description, price, stock";
            }

            List<Product> products = new ArrayList<>();
            String[] row;

            while ((row = reader.readNext()) != null) {
                Product product = new Product();
                product.setName(row[nameIndex].trim());
                product.setDescription(row[descriptionIndex].trim());
                try {
                    product.setPrice(Double.parseDouble(row[priceIndex].trim()));
                } catch (NumberFormatException e) {
                    product.setPrice(0.0);
                }
                try {
                    product.setStock(Integer.parseInt(row[stockIndex].trim()));
                } catch (NumberFormatException e) {
                    product.setStock(0);
                }
                products.add(product);
            }

            productRepository.saveAll(products);
            return "Productos importados: " + products.size();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error al procesar el archivo CSV: " + e.getMessage();
        }
    }

    private String importProductsFromExcel(InputStream inputStream) {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return "El archivo Excel está vacío.";
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return "El archivo Excel no tiene cabecera.";
            }

            int columns = headerRow.getPhysicalNumberOfCells();
            int nameIdx = -1, descIdx = -1, priceIdx = -1, stockIdx = -1;

            for (int i = 0; i < columns; i++) {
                String value = headerRow.getCell(i).getStringCellValue().trim().toLowerCase();
                switch (value) {
                    case "name":
                        nameIdx = i;
                        break;
                    case "description":
                        descIdx = i;
                        break;
                    case "price":
                        priceIdx = i;
                        break;
                    case "stock":
                        stockIdx = i;
                        break;
                }
            }

            if (nameIdx == -1 || descIdx == -1 || priceIdx == -1 || stockIdx == -1) {
                return "El archivo no tiene las columnas requeridas: name, description, price, stock";
            }

            List<Product> products = new ArrayList<>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null)
                    continue;

                Product product = new Product();
                product.setName(getCellString(row, nameIdx));
                product.setDescription(getCellString(row, descIdx));
                product.setPrice(getCellDouble(row, priceIdx));
                product.setStock((int) getCellDouble(row, stockIdx));
                products.add(product);
            }

            productRepository.saveAll(products);
            return "Productos importados: " + products.size();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "Error procesando el archivo Excel: " + ex.getMessage();
        }
    }

    private String getCellString(Row row, int idx) {
        Cell cell = row.getCell(idx);
        if (cell == null)
            return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
            default:
                return "";
        }
    }

    private double getCellDouble(Row row, int idx) {
        Cell cell = row.getCell(idx);
        if (cell == null)
            return 0.0;
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue());
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    @Override
    public List<Product> findAll() {
        return (productRepository.findAll().stream().map(product -> {
            product.setPort(Integer.parseInt(environment.getProperty("local.server.port")));
            return product;
        }).collect(Collectors.toList()));
    }

    @Override
    public Optional<Product> findById(String id) {
        return productRepository.findById(id);
    }

    @Override
    public Product create(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Optional<Product> update(String id, Product product) {
        return productRepository.findById(id)
                .map(existing -> {
                    existing.setName(product.getName());
                    existing.setDescription(product.getDescription());
                    existing.setPrice(product.getPrice());
                    existing.setStock(product.getStock());
                    return productRepository.save(existing);
                });
    }

    @Override
    public boolean delete(String id) {
        if (!productRepository.existsById(id))
            return false;
        productRepository.deleteById(id);
        return true;
    }

}
