package com.threeboys.toneup.chatbot.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.threeboys.toneup.personalColor.infra.FastApiClient;
import com.threeboys.toneup.product.domain.Product;
import com.threeboys.toneup.product.dto.ProductEmbedding;
import com.threeboys.toneup.product.dto.ProductEmbeddingRequest;
import com.threeboys.toneup.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingOptionsBuilder;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.json.Path2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.springframework.ai.vectorstore.redis.RedisVectorStore.DEFAULT_EMBEDDING_FIELD_NAME;
//@Component
//@RequiredArgsConstructor
//public class ChatbotInitialization implements CommandLineRunner {
//    private final ProductRepository productRepository;
//    private final FastApiClient fastApiClient;
//
//    private final VectorStore vectorStore;
//
//    private static final String EMBEDDING_FILE = "qna_embeddings.json";
//    private static final String PRODUCT_EMBEDDING_FILE = "product_embeddings.json";
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    public List<Document> loadDocumentsFromFile() {
//        try {
//            ClassPathResource resource = new ClassPathResource(EMBEDDING_FILE);
//            File file = resource.getFile();
//
//            if (!file.exists()) {
//                System.out.println(file.toPath() + ":  file PAth");
//                throw new RuntimeException("임베딩 파일이 존재하지 않습니다: " + EMBEDDING_FILE);
//            }
//
//            // 파일 읽기
//            String json = Files.readString(file.toPath());
//
//            // JSON -> List<Document>
//            return objectMapper.readValue(
//                    json, new TypeReference<List<Document>>() {}
//            );
//
//        } catch (Exception e) {
//            throw new RuntimeException("Document 파일 로드 실패", e);
//        }
//    }
//
//    //chatbot QnA 관련 문서 임베딩 후 벡터 스토어 저장 과정
//    @Override
//    public void run(String... args) throws Exception {
//        List<Document> documents = loadDocumentsFromFile();
//        loadEmbeddingFromFile();
//        vectorStore.add(documents);
//        //pipline 으로 배치 삽입 진행하여 레디스에 저장
//
//    }
//
//    private void loadEmbeddingFromFile() throws IOException {
//        //파일이 존재 하면 가져와서
//
//        //파일이 존재 하지 않으면
//        //fastapi 서버에 상품 데이터 전송해 임베딩 파일 받기
//
//        ClassPathResource resource = new ClassPathResource(PRODUCT_EMBEDDING_FILE);
//        try {
//
////            if (!resource.getFile().exists()) {
////
////            }
//
//            File file = resource.getFile();
//            System.out.println(file.toPath() + ":  file PAth");
//            // 파일 읽기
////            String json = Files.readString(file.toPath());
//
//            // JSON -> List<Document>
////            List<Document> documents = objectMapper.readValue(
////                    json, new TypeReference<List<Document>>() {}
////            );
//
//
//        } catch (Exception e) {
//            List<ProductEmbeddingRequest> products = productRepository.findAllEmbeddingData();
//            Resource embeddingResource = fastApiClient.downloadEmbeddingFile(products);
//
//            createEmbeddingFile(embeddingResource);
//            File file = resource.getFile();
//            System.out.println(file.toPath() + ":  file PAth , 파일 생성 완료");
////            throw new RuntimeException("Document 파일 로드 실패", e);
//        }
//    }
//
//    private void createEmbeddingFile(Resource resource) {
//
//        if (resource == null) {
//            throw new RuntimeException("파일 다운로드 실패");
//        }
//
//        // 파일명 설정 (FastAPI에서 Content-Disposition 헤더 자동 전달)
//        File targetFile = new File(PRODUCT_EMBEDDING_FILE);
//
//
//        try (InputStream inputStream = resource.getInputStream();
//             FileOutputStream outputStream = new FileOutputStream(targetFile)) {
//            inputStream.transferTo(outputStream);
//        }catch (IOException e){
//            System.out.println("파일 생성 실패 : "+ e.getMessage());
//        }
//
//        System.out.println("파일 저장 완료: " + targetFile.getAbsolutePath());
//    }
//}
@Component
//@RequiredArgsConstructor
public class ChatbotInitialization implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final FastApiClient fastApiClient;
    private final VectorStore vectorStore;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final JedisPooled jedis;

    private static final String QNA_EMBEDDING_FILE = "qna_embeddings.json";
    private static final String PRODUCT_EMBEDDING_FILE = "src/main/resources/product_embeddings.json";

    private static final Predicate<Object> RESPONSE_OK = Predicate.isEqual("OK");
    private static final Path2 JSON_SET_PATH = Path2.of("$");
    public static final String PRODUCT_PREFIX = "productEmbedding";

    public ChatbotInitialization(ProductRepository productRepository, FastApiClient fastApiClient, @Qualifier("openAiVectorStore") VectorStore vectorStore, JedisPooled jedis) {
        this.productRepository = productRepository;
        this.fastApiClient = fastApiClient;
        this.vectorStore = vectorStore;
        this.jedis = jedis;
    }

    @Override
    public void run(String... args) throws Exception {
        // QnA 문서 로드
        List<Document> documents = loadDocumentsFromFile();
        vectorStore.add(documents);

        // 상품 임베딩 로드 (없으면 FastAPI에서 받아오기)
//        loadEmbeddingFromFile();

        //redis pipeline으로 임베딩 데이터 백터 스토어에 저장
//        Path path = Paths.get(PRODUCT_EMBEDDING_FILE);

//        if (!Files.exists(path)) {
//            throw new RuntimeException("product 임베딩 파일이 존재하지 않습니다: " + path.toAbsolutePath());
//        }

//        String json = Files.readString(path);
//        List<ProductEmbedding> products =  objectMapper.readValue(
//                json,
//                new TypeReference<List<ProductEmbedding>>() {}
//        );
//        saveProductEmbeddingsWithPipeline(products);

    }

    private void saveProductEmbeddingsWithPipeline(List<ProductEmbedding> products) {
        try (Pipeline pipeline = this.jedis.pipelined()) {
            int count = 0;
            for (ProductEmbedding product : products) {
//                if(count==10) break;
                var fields = new HashMap<String, Object>();
                fields.put(DEFAULT_EMBEDDING_FIELD_NAME, product.getEmbedding());
//                fields.put(this.contentFieldName, document.getText());
//                fields.putAll(document.getMetadata());
                pipeline.jsonSetWithEscape(key(product.getProductId().toString()), JSON_SET_PATH, fields);
                count++;
            }
            List<Object> responses = pipeline.syncAndReturnAll();
            Optional<Object> errResponse = responses.stream().filter(Predicate.not(RESPONSE_OK)).findAny();
            if (errResponse.isPresent()) {
                String message = MessageFormat.format("Could not add document: {0}", errResponse.get());
//                if (logger.isErrorEnabled()) {
//                    logger.error(message);
//                }
                throw new RuntimeException(message);
            }
        }
    }
    private String key(String productId) {
        return PRODUCT_PREFIX+ ":" + productId;
    }

    private List<Document> loadDocumentsFromFile() {
        try {
            ClassPathResource resource = new ClassPathResource(QNA_EMBEDDING_FILE);


            if (!resource.exists()) {
                throw new RuntimeException("임베딩 파일이 존재하지 않습니다: " + resource.getPath());
            }
            // InputStream으로 읽어서 ObjectMapper에 전달
            InputStream is = resource.getInputStream();
                return objectMapper.readValue(is, new TypeReference<List<Document>>() {});
//            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Document 파일 로드 실패", e);
        }
    }

    private void loadEmbeddingFromFile() throws IOException {
        Path path = Paths.get(PRODUCT_EMBEDDING_FILE);


        // 파일이 없으면 FastAPI 호출해서 생성
        if (!Files.exists(path)) {
            System.out.println("상품 임베딩 파일 없음 → FastAPI에서 다운로드 중...");
            List<ProductEmbeddingRequest> products = productRepository.findAllEmbeddingData();
            Resource embeddingResource = fastApiClient.downloadEmbeddingFile(products);
            createEmbeddingFile(embeddingResource, path);
        }

        // JSON 파일 파싱
//        String json = Files.readString(path);
//        List<Document> documents = objectMapper.readValue(json, new TypeReference<>() {});
//        System.out.println("상품 임베딩 로딩 완료: " + documents.size() + "개");
    }

    private void createEmbeddingFile(Resource resource, Path path) {
        if (resource == null) {
            throw new RuntimeException("파일 다운로드 실패");
        }

        try {
            Files.createDirectories(path.getParent());
            try (InputStream inputStream = resource.getInputStream()) {
                Files.copy(inputStream, path);
            }
            System.out.println("상품 임베딩 파일 저장 완료: " + path.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("파일 생성 실패: " + e.getMessage(), e);
        }
    }
}
