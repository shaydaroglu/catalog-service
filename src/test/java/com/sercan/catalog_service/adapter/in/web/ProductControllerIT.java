package com.sercan.catalog_service.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sercan.catalog_service.adapter.out.persistence.ProductEntity;
import com.sercan.catalog_service.adapter.out.persistence.ProductJpaRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductControllerIT {

    private static final String PRODUCT_URL = "/api/v1/products";

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ProductJpaRepository jpaRepository;

    private ProductEntity savedProduct1;
    private ProductEntity savedProduct2;

    @BeforeEach
    void setUp() {
        jpaRepository.deleteAll();

        savedProduct1 = jpaRepository.save(ProductEntity.builder()
                .name("Product Offering 1")
                .price(BigDecimal.valueOf(10.00))
                .build());

        savedProduct2 = jpaRepository.save(ProductEntity.builder()
                .name("Product Offering 2")
                .price(BigDecimal.valueOf(20.00))
                .build());
    }

    @Nested
    @DisplayName("GET /api/v1/products")
    class FindAll {

        @Test
        @DisplayName("should return paginated products with default pagination")
        void shouldReturnPaginatedProducts() throws Exception {
            mockMvc.perform(get(PRODUCT_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(2)))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(20))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.totalPages").value(1));
        }

        @Test
        @DisplayName("should respect page and size parameters")
        void shouldRespectPaginationParams() throws Exception {
            mockMvc.perform(get(PRODUCT_URL)
                            .param("page", "0")
                            .param("size", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(1)))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.totalPages").value(2));
        }

        @Test
        @DisplayName("should return correct elements with pagination")
        void shouldReturnCorrectElementsWithPagination() throws Exception {
            mockMvc.perform(get("/api/v1/products")
                            .param("page", "0")
                            .param("size", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(1)))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.totalPages").value(2));

            mockMvc.perform(get("/api/v1/products")
                            .param("page", "1")
                            .param("size", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(1)))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.totalPages").value(2));
        }

        @Test
        @DisplayName("should return empty page when no products exist")
        void shouldReturnEmptyPage() throws Exception {
            jpaRepository.deleteAll();

            mockMvc.perform(get(PRODUCT_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("should return 400 when page is negative")
        void shouldReturn400WhenPageIsNegative() throws Exception {
            mockMvc.perform(get("/api/v1/products")
                            .param("page", "-1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("should return 400 when size exceeds maximum")
        void shouldReturn400WhenSizeExceedsMax() throws Exception {
            mockMvc.perform(get("/api/v1/products")
                            .param("size", "101"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("should return 400 when size is zero")
        void shouldReturn400WhenSizeIsZero() throws Exception {
            mockMvc.perform(get("/api/v1/products")
                            .param("size", "0"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/{id}")
    class FindById {

        @Test
        @DisplayName("should return product when found")
        void shouldReturnProductWhenFound() throws Exception {
            mockMvc.perform(get(PRODUCT_URL + "/{id}", savedProduct1.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(savedProduct1.getId().toString()))
                    .andExpect(jsonPath("$.name").value("Product Offering 1"))
                    .andExpect(jsonPath("$.price").value(10.00));
        }

        @Test
        @DisplayName("should return 404 with RFC 7807 body when product not found")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(get(PRODUCT_URL + "/{id}", UUID.randomUUID()))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType("application/problem+json"))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.title").value("Products Not Found"))
                    .andExpect(jsonPath("$.errors.missingIds").isArray());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/products/validate")
    class Validate {

        @Test
        @DisplayName("should return 200 when all ids exist")
        void shouldReturn200WhenAllIdsExist() throws Exception {
            List<UUID> ids = List.of(savedProduct1.getId(), savedProduct2.getId());

            mockMvc.perform(post("/api/v1/products/validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ids)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should return 404 with missing ids when some not found")
        void shouldReturn404WhenSomeIdsNotFound() throws Exception {
            UUID missingId = UUID.randomUUID();
            List<UUID> ids = List.of(savedProduct1.getId(), missingId);

            mockMvc.perform(post("/api/v1/products/validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ids)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType("application/problem+json"))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.errors.missingIds", hasItem(missingId.toString())));
        }

        @Test
        @DisplayName("should return 404 when all ids not found")
        void shouldReturn404WhenAllIdsNotFound() throws Exception {
            List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID());

            mockMvc.perform(post("/api/v1/products/validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ids)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors.missingIds", hasSize(2)));
        }

        @Test
        @DisplayName("should return 400 when empty list sent")
        void shouldReturn400WhenEmptyList() throws Exception {
            mockMvc.perform(post("/api/v1/products/validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[]"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType("application/problem+json"))
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("should return 400 when no body sent")
        void shouldReturn400WhenNoBody() throws Exception {
            mockMvc.perform(post("/api/v1/products/validate")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }
}
