package com.threeboys.toneup.chatbot.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.threeboys.toneup.chatbot.dto.BotMessageType;
import com.threeboys.toneup.chatbot.dto.ChatBotNotRocommandCodiResponse;
import com.threeboys.toneup.chatbot.dto.ChatBotRecommandCodiResponse;
import com.threeboys.toneup.chatbot.dto.ChatBotRequest;
import com.threeboys.toneup.common.domain.ImageType;
import com.threeboys.toneup.common.domain.Images;
import com.threeboys.toneup.common.repository.ImageRepository;
import com.threeboys.toneup.common.service.FileService;
import com.threeboys.toneup.like.domain.ProductsLike;
import com.threeboys.toneup.like.repository.ProductsLikeRepository;
import com.threeboys.toneup.product.domain.Product;
import com.threeboys.toneup.product.dto.ProductEmbedding;
import com.threeboys.toneup.product.exception.ProductNotFoundException;
import com.threeboys.toneup.product.repository.ProductRepository;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.exception.UserNotFoundException;
import com.threeboys.toneup.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.client.RedisClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.content.Media;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.*;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionConverter;
import org.springframework.ai.vectorstore.redis.RedisFilterExpressionConverter;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.json.Path2;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.RediSearchUtil;
import redis.clients.jedis.search.SearchResult;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.threeboys.toneup.chatbot.service.ChatbotInitialization.PRODUCT_PREFIX;
import static org.springframework.ai.vectorstore.redis.RedisVectorStore.*;

@Service
//@RequiredArgsConstructor
public class ChatbotService {
    private final OpenAiChatModel openAiChatModel;
    private final OpenAiEmbeddingModel openAiEmbeddingModel;
    private final OpenAiImageModel openAiImageModel;
    private final OpenAiAudioSpeechModel openAiAudioSpeechModel;
    private final OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;
    private final UserRepository userRepository;
    private final ProductsLikeRepository productsLikeRepository;
    private final ProductRepository productRepository;
    private final ImageRepository imageRepository;
    private final ChatMemoryRepository chatMemoryRepository;
    private final FileService fileService;

//    private final VectorStore vectorStore;
    private final VectorStore openAiVectorStore;
    private final VectorStore openClipVectorStore;

    private static final String OPENCLIP_INDEX_NAME = "spring-ai-index-openclip";
    private static final String QUERY_FORMAT = "%s=>[KNN %s @%s $%s AS %s]";
    private static final String EMBEDDING_PARAM_NAME = "BLOB";
    private final List<MetadataField> metadataFields = new ArrayList<>();
    private final FilterExpressionConverter filterExpressionConverter = new RedisFilterExpressionConverter(metadataFields);

    public ChatbotService(OpenAiChatModel openAiChatModel, OpenAiEmbeddingModel openAiEmbeddingModel, OpenAiImageModel openAiImageModel, OpenAiAudioSpeechModel openAiAudioSpeechModel, OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel, UserRepository userRepository, ProductsLikeRepository productsLikeRepository, ProductRepository productRepository, ImageRepository imageRepository, ChatMemoryRepository chatMemoryRepository, FileService fileService, @Qualifier("openAiVectorStore") VectorStore openAiVectorStore, @Qualifier("openClipVectorStore") VectorStore openClipVectorStore) {
        this.openAiChatModel = openAiChatModel;
        this.openAiEmbeddingModel = openAiEmbeddingModel;
        this.openAiImageModel = openAiImageModel;
        this.openAiAudioSpeechModel = openAiAudioSpeechModel;
        this.openAiAudioTranscriptionModel = openAiAudioTranscriptionModel;
        this.userRepository = userRepository;
        this.productsLikeRepository = productsLikeRepository;
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.chatMemoryRepository = chatMemoryRepository;
        this.fileService = fileService;
        this.openAiVectorStore = openAiVectorStore;
        this.openClipVectorStore = openClipVectorStore;
    }

//    private final ChatClient chatClient;

    public Object generateStream(ChatBotRequest chatbotRequest, Long userId) throws IOException {
        UserEntity user = userRepository.findById(userId).orElseThrow(() ->new UserNotFoundException(userId));
        String personalColor = user.getPersonalColor().getPersonalColorType().toString();
        ChatClient chatClient = ChatClient.create(openAiChatModel);
        List<String> buttonList = List.of(BotMessageType.EVALUATE_CODI.getTypeName(),BotMessageType.QnA.getTypeName(),BotMessageType.RECOMMEND_CODI.getTypeName());


        if(chatbotRequest.getBotMessageType().equals(BotMessageType.READY)){

            String messageResponse = """
                    안녕하세요! 먼저 원하시는 항목을 골라주세요!
                    무엇을 도와드릴까요?""";
            return new ChatBotNotRocommandCodiResponse(messageResponse,buttonList);
        }
        if (chatbotRequest.getBotMessageType().name().equals("EVALUATE_CODI")) {

            //            너는 퍼스널 컬러 전문가이면서 패션 전문가야.
            //            반드시 사용자의 퍼스널 컬러도 고려해서 사용자가 올린 코디 이미지를 분석해서 전체적인 분위기, 어울리는 색상, 개선할 점을 알려줘.
            //

            // 시스템 메시지
            SystemMessage systemMessage = new SystemMessage("""
            Role:
            당신은 퍼스널 컬러와 패션 스타일링 전문가입니다. 사용자가 업로드한 의상 코디 이미지를 분석하고, 제공된 퍼스널 컬러(봄웜/여름쿨/가을웜/겨울쿨)과의 조화를 평가하며, 스타일링 개선 팁을 제공합니다.
           \s
            Goal:
            사용자에게 상세하고 친근하며 이해하기 쉬운 패션 조언을 제공하는 것입니다. 퍼스널 컬러와 조화되는 색상과 톤을 강조하고, 착용 시 인상 변화까지 안내합니다.
           \s
            Target Audience:
            퍼스널 컬러를 기준으로 옷을 잘 고르고 싶은 일반 사용자, 패션/뷰티에 관심 있는 소비자
           \s
            Instructions:
            1. 사용자 입력 받기:
               - 업로드된 이미지
               - 퍼스널 컬러(선택 사항)
           \s
            2. 이미지 분석:
               - 의상에서 **주요 색상 추출**
               - 각 색상의 **톤**(밝기, 채도, 명도) 분석
               - **색상 팔레트 생성** (hex 코드 또는 색상 이름)
           \s
            3. 퍼스널 컬러 매칭 평가:
               - 사용자가 퍼스널 컬러 제공 시:
                 - 코디 색상과 사용자의 퍼스널 컬러 비교
                 - 매칭 결과 평가: "Excellent Match", "Good", "Needs Improvement"
                 - 평가 이유 설명 (톤, 채도, 명도 중심)
                 - 퍼스널 컬러 의미: 단순 계절감이 아니라 **피부 톤과 조화**
               - 사용자가 퍼스널 컬러 미제공 시:
                 - 제한 안내: "퍼스널 컬러를 알려주시면 더 정확한 평가가 가능합니다"
                 - 일반 패션 조언 제공 (보편적 색상/스타일 팁)
           \s
            4. 개선 및 스타일링 팁:
               - 매칭이 "Good" 또는 "Excellent"일 때: 칭찬 + 소소한 개선 팁
               - 매칭이 "Needs Improvement"일 때: 구체적 대체 색상, 스타일링 조언
               - 제안 사항에 **착용 시 얼굴톤/인상 변화** 포함
               - 예: 색상 변경 → 피부톤이 화사해 보임, 액세서리 변경 → 세련됨
           \s
            5. 응답 구조:
               - Markdown 사용
               - 색상 팔레트 시각화 (🎨 + hex 코드)
               - 퍼스널 컬러 매칭 평가
               - 스타일링 분석 (상세 항목별)
               - 개선 팁 (bullet point)
           \s
            Example Outputs:
           \s
            Example 1: Good Match
            코디 색상 팔레트: 🎨 #F08080 (코랄 핑크), #ADD8E6 (스카이 블루), #FFFFFF (화이트)
            퍼스널 컬러 매칭 평가: "봄웜 라이트" 타입에 아주 잘 맞는 화사한 코디입니다.
            전체적으로 밝고 청량한 느낌이 잘 살아나, 봄웜 라이트가 가진 생기 있고 화사한 이미지를 극대화해줍니다. 특히, 코랄 핑크와 스카이 블루의 조합이 얼굴빛을 더욱 맑고 생기 있게 보이게 하네요.
            스타일링 분석:
            상의 (코랄 핑크): 봄웜 라이트의 팔레트 중에서도 밝고 따뜻한 기운의 코랄이 얼굴에 혈색을 더해줍니다.
            하의 (스카이 블루): 채도가 낮아 부드러운 스카이 블루가 전체적인 룩에 시원하고 안정적인 균형을 잡아주네요.
            액세서리 (화이트): 화이트는 깨끗한 느낌을 더하며 코디의 밝은 무드를 한층 더 끌어올려줍니다.
            개선 팁:
            현재 코디를 충분히 잘 소화하고 계세요! 굳이 개선할 점을 찾자면, 슈즈나 가방 같은 아이템에 골드 톤을 살짝 더해준다면 봄웜 타입의 화사함이 더욱 돋보일 거예요.
           \s
            Example 2: Needs Improvement
             코디 색상 팔레트: 🎨 #6B8E23 (올리브 그린), #A9A9A9 (다크 그레이), #800000 (버건디)
             퍼스널 컬러 매칭 평가: "겨울쿨" 타입에 비해 현재 코디는 전체적으로 탁하고 채도가 낮은 편이라 얼굴이 다소 어두워 보일 수 있어요. 겨울쿨은 명확하고 선명한 색을 사용해야 매력이 살아납니다.
             스타일링 분석:
             상의 (올리브 그린): 올리브 그린은 가을 웜톤에 잘 어울리는 색상으로, 겨울쿨 타입에게는 피부 톤을 칙칙하게 만들 수 있습니다.
             하의 (다크 그레이): 명도가 낮고 탁한 그레이는 겨울쿨의 선명함을 가려 오히려 답답해 보일 수 있어요.
             액세서리 (버건디): 버건디 역시 채도가 낮아 강렬한 인상을 주기 어렵습니다.
             개선 팁:
             올리브 그린 대신 차가운 기운의 에메랄드 그린이나 라벤더를 활용해 보세요. → 훨씬 깨끗하고 투명한 피부를 연출할 수 있습니다.
             다크 그레이 대신 명도가 높은 코발트 블루나 블랙 팬츠를 매치하면 전체적으로 룩이 시원하고 선명해 보입니다. → 겨울쿨의 도회적이고 시크한 매력이 살아납니다.
             버건디 액세서리 대신 루비 레드나 실버 톤의 포인트를 추가하면 더욱 생동감 있는 코디가 됩니다.
           \s
            Constraints:
            - 전문 용어 사용 시 간단한 풀이 포함
            - 지나치게 기술적이지 않고 직관적, 세련되게 설명
            - 퍼스널 컬러는 **피부 톤 기반**, 계절감 의미 아님

       \s""");

            // Media (Base64 이미지)
            Media imageMedia = Media.builder()
                    .mimeType(MimeTypeUtils.IMAGE_PNG)
                    .data(chatbotRequest.getImageBase64())
                    .build();

            // UserMessage (텍스트 + 이미지 + metadata)
//            UserMessage userMessage = new UserMessage(
//                    "이 코디를 분석해줘. 참고: 내 퍼스널 컬러는 " + personalColor + "이야.",
//                    List.of(imageMedia),
//                    Map.of("personalColor", personalColor) // 로깅/추적용 metadata
//            );
            UserMessage userMessage = UserMessage.builder()
                    .media(imageMedia)
                    .metadata(Map.of("personalColor", personalColor)) // 로깅/추적용 metadata
                    .text("이 코디를 분석해줘. 퍼스널 컬러는 " + personalColor + "이야.")
                    .build();


            // 모델 옵션
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .model("gpt-4o-mini")
                    .temperature(0.7)
                    .build();
//            PromptTemplate
            Prompt prompt = new Prompt(List.of(systemMessage, userMessage), options);
//            chatClient.prompt().user((spec) -> spec.text(prompt).media(
//                    MimeTypeUtils.parseMimeType(mimeType), new InputStreamResource(inputStream)
//            )).call().chatClientResponse();

            // 응답 메시지를 저장할 임시 버퍼
            StringBuilder responseBuffer = new StringBuilder();

            InputStreamResource inputStream = new InputStreamResource(chatbotRequest.getImageFile().getInputStream());

            String messageResponse = chatClient.prompt().user(promptUserSpec -> promptUserSpec.media(MimeTypeUtils.IMAGE_PNG, new InputStreamResource(inputStream)).text(systemMessage.getText()+userMessage.getText())).call().content();
            return  new ChatBotNotRocommandCodiResponse(messageResponse, buttonList);


        }else if(chatbotRequest.getBotMessageType().name().equals("QnA")&chatbotRequest.getContent()!=null){
//            //1. 유사도 검색
//            List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
//                            .query(chatbotRequest.getContent())
//                            .topK(5)
//                    .build());
//            // 2. 검색된 문서들을 context로 묶기
//            String context = documents.stream()
//                    .map(Document::getText)
//                    .collect(Collectors.joining("\n"));
//
//            // 3. ChatClient 호출
//            return chatClient.prompt()
//                    .user("다음 정보를 기반으로 답해: \n" + context + "\n\n질문: " + chatbotRequest.getContent())
//                    .call()
//                    .content();
            String userQuestion = chatbotRequest.getContent();


            // 1. 시스템 메시지 (역할 및 규칙 정의)
            Message systemMessage = new SystemMessage("""
                당신은 패션 전문가입니다.
                규칙:
                1. 사용자의 질문이 옷, 패션, 퍼스널컬러, 메이크업과 관련되면 구체적이고 친절하게 답변하세요.
                2. 관련이 없는 질문이라면 무조건 다음 문장으로만 답변하세요:
                   "옷, 패션, 퍼스널컬러, 메이크업 등 이와 관련된 질문을 해주시면 감사하겠습니다."
            """);
            ChatMemory chatMemory = MessageWindowChatMemory.builder()
                    .maxMessages(10)
                    .chatMemoryRepository(chatMemoryRepository)
                    .build();
            Message userMessage = new UserMessage(userQuestion);
            chatMemory.add(userId.toString(), userMessage);

            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .model("gpt-4o-mini")
                    .temperature(0.7)
                    .build();

            Prompt prompt = new Prompt(chatMemory.get(userId.toString()), options);

            // 5. ChatClient 호출
            String messageResponse =  chatClient.prompt(prompt)
                    .call()
                    .content();
            chatMemory.add(userId.toString(), new AssistantMessage(Objects.requireNonNull(messageResponse)));

            return new ChatBotNotRocommandCodiResponse(messageResponse, List.of(BotMessageType.READY.getTypeName(), BotMessageType.QnA.getTypeName()+" 끝내기"));
        } else if (chatbotRequest.getBotMessageType().equals(BotMessageType.RECOMMEND_CODI)) {
            List<ProductsLike> userLikeProductList = productsLikeRepository.findTop5ByUserIdOrderByIdDesc(userId);

            Optional<JedisPooled> nativeClient = openClipVectorStore.getNativeClient();

            List<SearchResult> resultList= new ArrayList<>();
            if (nativeClient.isPresent()) {
                JedisPooled jedis = nativeClient.get();
                SearchRequest request = SearchRequest.builder()
                        .query("not necessary query")// 임베딩 벡터 아래에서 따로 조회함
                        .topK(3)
                        .build();
                String filter = nativeExpressionFilter(request);

                String queryString = String.format(QUERY_FORMAT, filter, request.getTopK(), DEFAULT_EMBEDDING_FIELD_NAME,
                        EMBEDDING_PARAM_NAME, DISTANCE_FIELD_NAME);

                List<String> returnFields = new ArrayList<>();
//                this.metadataFields.stream().map(RedisVectorStore.MetadataField::name).forEach(returnFields::add);
                returnFields.add(DEFAULT_EMBEDDING_FIELD_NAME);
//                returnFields.add(this.contentFieldName);
                returnFields.add(DISTANCE_FIELD_NAME);

                for(ProductsLike productLike : userLikeProductList){
                    ObjectMapper objectMapper = new ObjectMapper();
//                    Object json = jedis.jsonGet("product:" + productLike.getProduct().getId());
//
//                    System.out.println(json +" : json!!!!!!!!!!!!!!!!111");
                    Object raw = jedis.jsonGet(PRODUCT_PREFIX + ":" + productLike.getProduct().getId());
                    String json = objectMapper.writeValueAsString(raw);
                    ProductEmbedding embedding = objectMapper.readValue(json, ProductEmbedding.class);

                    Query query = new Query(queryString).addParam(EMBEDDING_PARAM_NAME, RediSearchUtil.toByteArray(embedding.getEmbedding()))
                            .returnFields(returnFields.toArray(new String[0]))
                            .setSortBy(DISTANCE_FIELD_NAME, true)
                            .limit(0, request.getTopK())
                            .dialect(2);

                    SearchResult result = jedis.ftSearch(OPENCLIP_INDEX_NAME, query);
                    resultList.add(result);
                }
                List<String> productPresignedUrlList = new ArrayList<>();
                List<String> productDetailHrefList = new ArrayList<>();
                for(SearchResult r : resultList){
//                    System.out.println(r.toString() + "결과 확인용");
                    String key = r.getDocuments().get(1).getId();
                    Long productId = Long.parseLong(key.substring(key.lastIndexOf(":") + 1));
                    Product product = productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);
                    List<Images> image = imageRepository.findByTypeAndRefId(ImageType.PRODUCT, productId);
                    productPresignedUrlList.add(fileService.getPreSignedUrl(image.getFirst().getS3Key()));
                    productDetailHrefList.add(product.getHerf());
                }
                return new ChatBotRecommandCodiResponse(productPresignedUrlList, productDetailHrefList, buttonList);
            }
        }

        return null;
    }

    private String nativeExpressionFilter(SearchRequest request) {
        if (request.getFilterExpression() == null) {
            return "*";
        }
        return "(" + this.filterExpressionConverter.convertExpression(request.getFilterExpression()) + ")";
    }


}

