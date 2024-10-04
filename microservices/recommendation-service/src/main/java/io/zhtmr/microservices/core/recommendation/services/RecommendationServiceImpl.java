package io.zhtmr.microservices.core.recommendation.services;

import io.zhtmr.api.core.recommendation.Recommendation;
import io.zhtmr.api.core.recommendation.RecommendationService;
import io.zhtmr.api.exceptions.InvalidInputException;
import io.zhtmr.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RecommendationServiceImpl implements RecommendationService {
  private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);

  private final ServiceUtil serviceUtil;

  public RecommendationServiceImpl(ServiceUtil serviceUtil) {
    this.serviceUtil = serviceUtil;
  }

  @Override
  public List<Recommendation> getRecommendations(int productId) {
    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }

    if (productId == 113) {
      LOG.debug("발견된 추천목록 없음 productId: {}", productId);
      return new ArrayList<>();
    }

    List<Recommendation> list = new ArrayList<>();
    list.add(new Recommendation(productId, 1, "Author 1", 1, "content 1", serviceUtil.getServiceAddress()));
    list.add(new Recommendation(productId, 2, "Author 2", 2, "content 2", serviceUtil.getServiceAddress()));
    list.add(new Recommendation(productId, 3, "Author 3", 3, "content 3", serviceUtil.getServiceAddress()));

    LOG.debug("/recommendation response size: {}", list.size());

    return list;
  }
}
