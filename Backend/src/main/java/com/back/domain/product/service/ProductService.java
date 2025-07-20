package com.back.domain.product.service;

import com.back.domain.product.controller.AdmProductController;
import com.back.domain.product.entity.Product;
import com.back.domain.product.repository.ProductRepository;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    @Value("${custom.gcp.bucket}")
    private String bucketName;
    private String defaultImageLabel = "product_name";

    //google 저장소 객체생성
    private Storage getStorage() throws IOException {
        String keyFileName = "cafeimagestorage-e894a0d38084.json";
        InputStream keyFile = ResourceUtils.getURL("classpath:" + keyFileName).openStream();

        return StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(keyFile))
                .build()
                .getService();
    }

    //파일업로드 및 url생성
    private String uploadFileToGCS(MultipartFile file, String fileName) throws IOException {
        Storage storage = getStorage();

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getInputStream());

        return "https://storage.googleapis.com/" + bucketName + "/" + fileName;
    }

    //수정시 이미지 업로드
    public String imageUpload(MultipartFile file, long id) throws IOException {
        String fileName = id + defaultImageLabel;
        return uploadFileToGCS(file, fileName);
    }

    //상품생성시
    public Product uploadObject(AdmProductController.GCSReqBody reqBody, MultipartFile file) throws IOException {
        // 1. 우선 상품을 imageUrl 없이 저장
        Product product = create(
                reqBody.productName(),
                reqBody.price(),
                "", // 이미지 URL은 비워둔다
                reqBody.category(),
                reqBody.description(),
                reqBody.orderable()
        );

        // 2. 파일이 비어있으면 바로 반환
        if (file == null || file.isEmpty() || file.getSize() < 100) {
            return product;
        }
        String fileName = product.getId() + defaultImageLabel;
        String imageUrl = uploadFileToGCS(file, fileName);

        product.setImageUrl(imageUrl);
        return productRepository.save(product);
    }


    public Product create(String productName, int price, String imageUrl,
                          String category, String description, boolean orderable) {

        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("상품명은 필수입니다.");
        }
        if (price < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        }

        return productRepository.save(
                Product
                        .builder()
                        .productName(productName)
                        .price(price)
                        .imageUrl(imageUrl)
                        .category(category)
                        .description(description)
                        .orderable(orderable)
                        .build()
        );

    }

    //컨트롤러 에서 유효성 검증함 , 여기서 생략
    public Page<Product> getItems(int page, int pageSize) {
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize);

        return productRepository.findAll(pageRequest);
    }

    public long count() {
        return productRepository.count();
    }

    public Optional<Product> getItem(long id) {
        return productRepository.findById(id);
    }

    public Optional<Product> getLatestItem() {
        return productRepository.findTopByOrderByIdDesc(); //상품 id 기준으로 제일 최근 생성된거
    }

    @Transactional
    public void modifyImage(Product product, MultipartFile file) throws IOException {

        long id = product.getId();
        if (file != null && !file.isEmpty()) { //새로 파일 업로드하면, 새 url반환
            String targetUrl = imageUpload(file, id);
            product.setImageUrl(targetUrl);
        }
        //없으면 기존 이미지 유지

    }

    @Transactional
    public void modify(Product product, String productName, int price, String imageUrl,
                       String category, String description, boolean orderable) {
        product.setProductName(productName);
        product.setPrice(price);
        product.setImageUrl(imageUrl);
        product.setCategory(category);
        product.setDescription(description);
        product.setOrderable(orderable);
    }

    public void delete(Product product) {
        productRepository.delete(product);
    }

    public void updateOrderable(Product product, boolean orderable) {
        product.setOrderable(orderable);
    }
}
