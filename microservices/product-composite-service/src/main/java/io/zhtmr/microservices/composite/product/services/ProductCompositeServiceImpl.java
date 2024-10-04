package io.zhtmr.microservices.composite.product.services;

import io.zhtmr.api.composite.*;
import io.zhtmr.api.core.product.Product;
import io.zhtmr.api.core.recommendation.Recommendation;
import io.zhtmr.api.core.review.Review;
import io.zhtmr.api.exceptions.NotFoundException;
import io.zhtmr.util.http.ServiceUtil;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

  private final ServiceUtil serviceUtil;
  private ProductCompositeIntegration integration;

  public ProductCompositeServiceImpl(ServiceUtil serviceUtil,
      ProductCompositeIntegration integration) {
    this.serviceUtil = serviceUtil;
    this.integration = integration;
  }

  @Override
  public ProductAggregate getProduct(int productId) {
    Product product = integration.getProduct(productId);
    if (product == null) {
      throw new NotFoundException("제품 정보 없음 : " + productId);
    }

    List<Recommendation> recommendations = integration.getRecommendations(productId);
    List<Review> reviews = integration.getReviews(productId);

    return createProductAggregate(product, recommendations, reviews,
        serviceUtil.getServiceAddress());
  }

  private ProductAggregate createProductAggregate(Product product, List<Recommendation> recommendations, List<Review> reviews, String serviceAddress) {
    int productId = product.getProductId();
    String name = product.getName();
    int weight = product.getWeight();

    List<RecommendationSummary> recommendationSummaries = (recommendations == null) ?
        null :
        recommendations.stream()
            .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate()))
            .collect(Collectors.toList());

    List<ReviewSummary> reviewSummaries = (reviews == null) ?
        null :
        reviews.stream().map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject()))
            .collect(Collectors.toList());

    String productAddress = product.getServiceAddress();
    String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
    String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
    ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

    return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
  }
}
