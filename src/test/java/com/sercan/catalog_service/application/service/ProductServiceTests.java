package com.sercan.catalog_service.application.service;

import com.sercan.catalog_service.application.port.out.ProductRepository;
import com.sercan.catalog_service.domain.Product;
import com.sercan.catalog_service.domain.exception.ProductsNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTests {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private UUID id1, id2, id3;
    private Product product1, product2, product3;

    @BeforeEach
    void setUp() {
        id1 = UUID.randomUUID();
        id2 = UUID.randomUUID();
        id3 = UUID.randomUUID();

        product1 = new Product(id1, "Product Offering 1", BigDecimal.valueOf(10.0000));
        product2 = new Product(id2, "Product Offering 2", BigDecimal.valueOf(20.0000));
        product3 = new Product(id3, "Product Offering 3", BigDecimal.valueOf(30.0000));
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return product when found")
        void shouldReturnProductWhenFound() {
            when(productRepository.findById(id1)).thenReturn(Optional.of(product1));

            Optional<Product> result = productService.findById(id1);

            assertThat(result).isPresent();
            assertThat(result.get().id()).isEqualTo(id1);
            assertThat(result.get().name()).isEqualTo("Product Offering 1");
            assertThat(result.get().price()).isEqualByComparingTo(BigDecimal.valueOf(10.0000));
        }

        @Test
        @DisplayName("should return empty when product not found")
        void shouldReturnEmptyWhenNotFound() {
            when(productRepository.findById(id1)).thenReturn(Optional.empty());

            Optional<Product> result = productService.findById(id1);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("verifyAllByIds")
    class VerifyAllByIds {

        @Test
        @DisplayName("should not throw exception when all ids of products exist")
        void shouldReturnAllProductsWhenAllIdsExist() {
            List<UUID> ids = List.of(id1, id2, id3);
            when(productRepository.findExistingIds(ids)).thenReturn(Set.of(id1, id2, id3));
            assertThatNoException().isThrownBy(() -> productService.verifyAllByIds(ids));
        }

        @Test
        @DisplayName("should throw ProductsNotFoundException with missing ids when some not found")
        void shouldThrowWhenSomeIdsNotFound() {
            UUID missingId = UUID.randomUUID();
            List<UUID> ids = List.of(id1, missingId);
            when(productRepository.findExistingIds(ids)).thenReturn(Set.of(id1));

            assertThatThrownBy(() -> productService.verifyAllByIds(ids))
                    .isInstanceOf(ProductsNotFoundException.class)
                    .satisfies(ex -> {
                        ProductsNotFoundException e = (ProductsNotFoundException) ex;
                        assertThat(e.getMissingIds()).containsExactly(missingId);
                    });
        }

        @Test
        @DisplayName("should throw ProductsNotFoundException with all ids when none found")
        void shouldThrowWhenNoIdsFound() {
            List<UUID> ids = List.of(id1, id2);
            when(productRepository.findExistingIds(ids)).thenReturn(Set.of());

            assertThatThrownBy(() -> productService.verifyAllByIds(ids))
                    .isInstanceOf(ProductsNotFoundException.class)
                    .satisfies(ex -> {
                        ProductsNotFoundException e = (ProductsNotFoundException) ex;
                        assertThat(e.getMissingIds()).containsExactlyInAnyOrder(id1, id2);
                    });
        }
    }

    @Nested
    @DisplayName("findAll paginated")
    class FindAll {

        @Test
        @DisplayName("should delegate pagination to repository")
        void shouldDelegatePaginationToRepository() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Product> page = new PageImpl<>(List.of(product1, product2, product3));

            when(productRepository.findAll(pageable)).thenReturn(page);

            Page<Product> result = productService.findAll(pageable);

            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(3);
            verify(productRepository, times(1)).findAll(pageable);
        }
    }
}
