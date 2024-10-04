package io.zhtmr.microservices.composite.product.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zhtmr.api.core.product.Product;
import io.zhtmr.api.core.product.ProductService;
import io.zhtmr.api.core.recommendation.Recommendation;
import io.zhtmr.api.core.recommendation.RecommendationService;
import io.zhtmr.api.core.review.Review;
import io.zhtmr.api.core.review.ReviewService;
import io.zhtmr.api.exceptions.InvalidInputException;
import io.zhtmr.api.exceptions.NotFoundException;
import io.zhtmr.util.http.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;

@Component
public class ProductCompositeIntegration
    implements ProductService, RecommendationService, ReviewService {

  private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

  private final RestTemplate restTemplate;
  private final ObjectMapper mapper;

  private final String productServiceUrl;
  private final String recommendationServiceUrl;
  private final String reviewServiceUrl;

  public ProductCompositeIntegration(
      RestTemplate restTemplate,
      ObjectMapper mapper,
      @Value("${app.product-service.host}") String productServiceHost,
      @Value("${app.product-service.port}") String productServicePort,
      @Value("${app.recommendation-service.host}") String recommendationServiceHost,
      @Value("${app.recommendation-service.port}") String recommendationServicePort,
      @Value("${app.review-service.host}") String reviewServiceHost,
      @Value("${app.review-service.port}") String reviewServicePort
  ) {
    this.restTemplate = restTemplate;
    this.mapper = mapper;
    this.productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product/";
    this.recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation?productId=";
    this.reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review?productId=";
  }

  @Override
  public Product getProduct(int productId) {
    try {
      String url = productServiceUrl + productId;
      LOG.debug("getProduct API 호출 URL: {}", url);

      Product product = restTemplate.getForObject(url, Product.class);
      LOG.debug("발견 product id: {}", product.getProductId());

      return product;

    } catch (HttpClientErrorException e) {
      switch (e.getStatusCode()) {
        case NOT_FOUND:
          throw new NotFoundException(getErrorMessage(e));
        case UNPROCESSABLE_ENTITY:
          throw new InvalidInputException(getErrorMessage(e));
        default:
          LOG.warn("예상치 못한 HTTP 에러 발생 : {}", e.getStatusCode());
          LOG.warn("Error body: {}", e.getResponseBodyAsString());
          throw e;
      }
    }
  }

  private String getErrorMessage(HttpClientErrorException e) {
    try {
      return mapper.readValue(e.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
    } catch (JsonProcessingException ioex) {
      return e.getMessage();
    }
  }

  @Override
  public List<Recommendation> getRecommendations(int productId) {
    try {
      String url = recommendationServiceUrl + productId;
      LOG.debug("getRecommendations API 호출 URL: {}", url);
      List<Recommendation> recommendations =
          restTemplate
              .exchange(url, GET, null, new ParameterizedTypeReference<List<Recommendation>>() {})
              .getBody();

      LOG.debug("{} 개의 추천항목을 찾았습니다. productId: {}", recommendations.size(), productId);
      return recommendations;

    } catch (Exception e) {
      LOG.warn("추천항목을 불러오는 중에 오류 발생: {}", e.getMessage());
      return new ArrayList<>();
    }
  }

  @Override
  public List<Review> getReviews(int productId) {
    try {
      String url = reviewServiceUrl + productId;

      LOG.debug("getReviews API 호출 URL: {}", url);
      List<Review> reviews =
          restTemplate
              .exchange(url, GET, null, new ParameterizedTypeReference<List<Review>>() {})
              .getBody();
      LOG.debug("{} 개의 리뷰를 찾았습니다. productId: {}", reviews.size(), productId);
      return reviews;
    } catch (Exception e) {
      LOG.warn("리뷰를 불러오는 중에 오류 발생: {}", e.getMessage());
      return new ArrayList<>();
    }
  }
}
