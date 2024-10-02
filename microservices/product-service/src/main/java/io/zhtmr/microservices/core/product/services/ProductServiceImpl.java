package io.zhtmr.microservices.core.product.services;

import io.zhtmr.api.core.product.Product;
import io.zhtmr.api.core.product.ProductService;
import io.zhtmr.api.exceptions.InvalidInputException;
import io.zhtmr.api.exceptions.NotFoundException;
import io.zhtmr.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductServiceImpl implements ProductService {
  private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

  private final ServiceUtil serviceUtil;

  public ProductServiceImpl(ServiceUtil serviceUtil) {
    this.serviceUtil = serviceUtil;
  }


  @Override
  public Product getProduct(int productId) {
    LOG.debug("/product 는 발견된 제품 id 를 반환한다. productId={}", productId);

    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }

    if (productId == 13) {
      throw new NotFoundException("발견된 제품없음 productId: " + productId);
    }

    return new Product(productId, "name-" + productId, 123, serviceUtil.getServiceAddress());
  }
}
