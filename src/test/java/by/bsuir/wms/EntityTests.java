package by.bsuir.wms;

import by.bsuir.wms.Entity.*;
import by.bsuir.wms.Entity.Enum.Role;
import by.bsuir.wms.Entity.Enum.Status;
import by.bsuir.wms.Repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@EnableAutoConfiguration
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class EntityTests {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private CellRepository cellRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private EmployeesRepository employeesRepository;

    @Test
    public void testSaveOrganization() {
        Organization organization = new Organization();
        organization.setINN("123456789");
        organization.setName("Test Organization");
        organization.setAddress("123 Test St");

        organizationRepository.save(organization);

        Organization found = organizationRepository.findById(organization.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("Test Organization", found.getName());
    }

    @Test
    public void testSaveProduct() {
        Product product = new Product();
        product.setName("Test Product");
        product.setHeight(10.0);
        product.setLength(20.0);
        product.setWidth(15.0);
        product.setWeight(1.5);
        product.setPrice(99.99);
        product.setUnit("pcs");
        product.setStatus(Status.accepted);
        product.setBestBeforeDate(Date.valueOf(LocalDate.now().plusMonths(6)));
        product.setAmount(100);

        productRepository.save(product);

        Product found = productRepository.findById(product.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("Test Product", found.getName());
    }

    @Test
    public void testSaveEmployee() {
        Organization organization = new Organization();
        organization.setINN("123456789");
        organization.setName("Test Organization");
        organization.setAddress("123 Test St");
        organizationRepository.save(organization);

        Warehouse warehouse = new Warehouse();
        warehouse.setName("Test Warehouse");
        warehouse.setAddress("123 Test St");
        warehouse.setOrganization(organization);
        warehouseRepository.save(warehouse);

        Employees employee = new Employees();
        employee.setLogin("test_user");
        employee.setPassword("password");
        employee.setFirstName("John");
        employee.setSecondName("Doe");
        employee.setSurname("Smith");
        employee.setPhone("+123456789");
        employee.setRole(Role.ROLE_WORKER);
        employee.setOrganization(organization);
        employee.setWarehouse(warehouse);

        employeesRepository.save(employee);

        Employees found = employeesRepository.findById(employee.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("John", found.getFirstName());
        assertEquals("Test Organization", found.getOrganization().getName());
        assertEquals("Test Warehouse", found.getWarehouse().getName());
    }

    @Test
    public void testSaveStockAndSupplier() {
        Supplier supplier = new Supplier();
        supplier.setInn("987654321");
        supplier.setName("Test Supplier");
        supplier.setAddress("456 Supplier St");
        supplierRepository.save(supplier);

        Product product = new Product();
        product.setName("Stocked Product");
        product.setHeight(10.0);
        product.setLength(20.0);
        product.setWidth(15.0);
        product.setWeight(1.5);
        product.setPrice(49.99);
        product.setUnit("kg");
        product.setStatus(Status.accepted);
        product.setBestBeforeDate(Date.valueOf(LocalDate.now().plusMonths(3)));
        product.setAmount(50);
        productRepository.save(product);

        Stock stock = new Stock();
        stock.setAmount(10);
        stock.setSupplier(supplier);
        stock.setProduct(product);
        stockRepository.save(stock);

        Stock foundStock = stockRepository.findById(stock.getId()).orElse(null);
        assertNotNull(foundStock);
        assertEquals("Stocked Product", foundStock.getProduct().getName());
        assertEquals("Test Supplier", foundStock.getSupplier().getName());
    }
}
