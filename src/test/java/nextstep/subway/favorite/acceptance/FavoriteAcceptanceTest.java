package nextstep.subway.favorite.acceptance;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.auth.dto.TokenRequest;
import nextstep.subway.auth.dto.TokenResponse;
import nextstep.subway.favorite.dto.FavoriteRequest;
import nextstep.subway.favorite.dto.FavoriteResponse;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.station.dto.StationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static nextstep.subway.auth.acceptance.AuthAcceptanceTest.로그인_성공;
import static nextstep.subway.auth.acceptance.AuthAcceptanceTest.로그인_요청;
import static nextstep.subway.line.acceptance.LineAcceptanceTest.지하철_노선_등록되어_있음;
import static nextstep.subway.member.MemberAcceptanceTest.회원_생성을_요청;
import static nextstep.subway.station.StationAcceptanceTest.지하철역_등록되어_있음;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("즐겨찾기 관련 기능")
public class FavoriteAcceptanceTest extends AcceptanceTest {
    private static final String EMAIL = "email@email.com";
    private static final String PASSWORD = "password";
    private static final int AGE = 20;

    private TokenResponse tokenResponse;
    private LineResponse 이호선;
    private StationResponse 강남역;
    private StationResponse 잠실역;

    @BeforeEach
    public void setUp() {
        super.setUp();
        회원_생성을_요청(EMAIL, PASSWORD, AGE);
        tokenResponse = 로그인_성공(로그인_요청(new TokenRequest(EMAIL, PASSWORD)));

        강남역 = 지하철역_등록되어_있음("강남역").as(StationResponse.class);
        잠실역 = 지하철역_등록되어_있음("잠실역").as(StationResponse.class);
        LineRequest lineRequest = new LineRequest("이호선", "green", 강남역.getId(), 잠실역.getId(), 10);
        이호선 = 지하철_노선_등록되어_있음(lineRequest).as(LineResponse.class);
    }

    @DisplayName("즐겨찾기를 생성한다.")
    @Test
    void createFavorite() {
        // when
        ExtractableResponse<Response> response = 즐겨찾기_등록되어_있음(tokenResponse, 강남역, 잠실역);

        // then
        즐겨찾기_생성됨(response);
    }

    @DisplayName("즐겨찾기를 조회한다.")
    @Test
    void findFavorites() {
        // given
        ExtractableResponse<Response> createResponse = 즐겨찾기_등록되어_있음(tokenResponse, 강남역, 잠실역);

        // when
        ExtractableResponse<Response> response = 즐겨찾기_조회_요청(tokenResponse);

        // then
        즐겨찾기_목록_응답됨(response);
        즐겨찾기_목록_포함됨(response, Collections.singletonList(createResponse));
    }

    @DisplayName("즐겨찾기를 제거한다.")
    @Test
    void deleteFavorite() {
        // given
        ExtractableResponse<Response> createResponse = 즐겨찾기_등록되어_있음(tokenResponse, 강남역, 잠실역);

        // when
        ExtractableResponse<Response> response = 즐겨찾기_제거_요청(tokenResponse, createResponse);

        // then
        즐겨찾기_삭제됨(response);
    }

    @DisplayName("다른 사람이 추가한 즐겨찾기 삭제 불가")
    @Test
    void deleteFavoriteException() {
        // given
        ExtractableResponse<Response> 즐겨찾기_생성됨 = 즐겨찾기_등록되어_있음(tokenResponse, 강남역, 잠실역);

        String NEW_EMAIL = "nextstep@test.com";
        String NEW_PASSWORD = "nextstep";
        회원_생성을_요청(NEW_EMAIL, NEW_PASSWORD, AGE);
        TokenResponse 다른계정_로그인 = 로그인_성공(로그인_요청(new TokenRequest(NEW_EMAIL, NEW_PASSWORD)));

        // when
        ExtractableResponse<Response> response = 즐겨찾기_제거_요청(다른계정_로그인, 즐겨찾기_생성됨);

        // then
        즐겨찾기_삭제실패함(response);
    }

    @DisplayName("시나리오 기반 인수테스트")
    @Test
    void scenario1() {
        // when: 즐겨찾기 생성을 요청
        ExtractableResponse<Response> 강남역_잠실역 = 즐겨찾기_등록되어_있음(tokenResponse, 강남역, 잠실역);

        // then: 즐겨찾기 생성됨
        즐겨찾기_생성됨(강남역_잠실역);

        // when: 즐겨찾기 목록 조회 요청
        ExtractableResponse<Response> 조회된_즐겨찾기_목록 = 즐겨찾기_조회_요청(tokenResponse);

        // then: 즐겨찾기 목록 조회됨
        즐겨찾기_목록_응답됨(조회된_즐겨찾기_목록);
        즐겨찾기_목록_포함됨(조회된_즐겨찾기_목록, Collections.singletonList(강남역_잠실역));

        // when: 새로운 노선 등록됨
        StationResponse 김포공항 = 지하철역_등록되어_있음("김포공항").as(StationResponse.class);
        StationResponse 여의도 = 지하철역_등록되어_있음("여의도").as(StationResponse.class);
        지하철_노선_등록되어_있음(new LineRequest("9호선", "gold", 김포공항.getId(), 여의도.getId(), 10)).as(LineResponse.class);

        // then: 즐겨찾기 생성을 요청
        ExtractableResponse<Response> 김포공항_여의도 = 즐겨찾기_등록되어_있음(tokenResponse, 김포공항, 여의도);

        // when: 즐겨찾기 목록 조회 요청
        ExtractableResponse<Response> 추가로_조회된_즐겨찾기_목록 = 즐겨찾기_조회_요청(tokenResponse);

        // then: 즐겨찾기 목록 조회됨
        즐겨찾기_목록_응답됨(추가로_조회된_즐겨찾기_목록);
        즐겨찾기_목록_포함됨(추가로_조회된_즐겨찾기_목록, Arrays.asList(강남역_잠실역, 김포공항_여의도));

        // when
        ExtractableResponse<Response> deleteResponse = 즐겨찾기_제거_요청(tokenResponse, 강남역_잠실역);

        // then
        즐겨찾기_삭제됨(deleteResponse);

        // when: 즐겨찾기 목록 조회 요청
        ExtractableResponse<Response> 삭제후_조회된_즐겨찾기_목록 = 즐겨찾기_조회_요청(tokenResponse);

        // then: 즐겨찾기 목록 조회됨
        즐겨찾기_목록_응답됨(삭제후_조회된_즐겨찾기_목록);
        즐겨찾기_목록_포함됨(삭제후_조회된_즐겨찾기_목록, Collections.singletonList(김포공항_여의도));

    }

    public static ExtractableResponse<Response> 즐겨찾기_등록되어_있음(TokenResponse tokenResponse, StationResponse source, StationResponse target) {
        FavoriteRequest favoriteRequest = new FavoriteRequest(source.getId(), target.getId());
        return RestAssured
                .given().log().all()
                .auth().oauth2(tokenResponse.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(favoriteRequest)
                .when().post("/favorites")
                .then().log().all().extract();
    }

    public static void 즐겨찾기_생성됨(ExtractableResponse response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.header("Location")).isNotBlank();
    }

    public static ExtractableResponse<Response> 즐겨찾기_조회_요청(TokenResponse tokenResponse) {
        return RestAssured
                .given().log().all()
                .auth().oauth2(tokenResponse.getAccessToken())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/favorites")
                .then().log().all()
                .extract();
    }

    public static void 즐겨찾기_목록_응답됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    public static void 즐겨찾기_목록_포함됨(ExtractableResponse<Response> response, List<ExtractableResponse<Response>> createdResponses) {
        List<Long> expectedLineIds = createdResponses.stream()
                .map(it -> Long.parseLong(it.header("Location").split("/")[2]))
                .collect(Collectors.toList());

        List<Long> resultLineIds = response.jsonPath().getList(".", FavoriteResponse.class).stream()
                .map(FavoriteResponse::getId)
                .collect(Collectors.toList());

        assertThat(resultLineIds).containsAll(expectedLineIds);
    }

    public static ExtractableResponse<Response> 즐겨찾기_제거_요청(TokenResponse tokenResponse, ExtractableResponse<Response> response) {
        String uri = response.header("Location");
        return RestAssured
                .given().log().all()
                .auth().oauth2(tokenResponse.getAccessToken())
                .when().delete(uri)
                .then().log().all()
                .extract();
    }

    public static void 즐겨찾기_삭제됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    public static void 즐겨찾기_삭제실패함(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }
}
