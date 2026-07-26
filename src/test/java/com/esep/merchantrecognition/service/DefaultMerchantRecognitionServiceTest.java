package com.esep.merchantrecognition.service;

import com.esep.merchantrecognition.interfaces.MerchantRecognitionService;
import com.esep.merchantresolver.interfaces.MerchantAliasCatalog;
import com.esep.merchantresolver.interfaces.MerchantCatalog;
import com.esep.merchantresolver.interfaces.MerchantResolver;
import com.esep.merchantresolver.repository.InMemoryMerchantAliasCatalog;
import com.esep.merchantresolver.repository.InMemoryMerchantRepository;
import com.esep.merchantresolver.service.InMemoryMerchantResolver;
import com.esep.normalization.interfaces.MerchantNormalizer;
import com.esep.normalization.service.DefaultMerchantNormalizer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultMerchantRecognitionServiceTest {

    private final MerchantRecognitionService recognitionService = createRecognitionService();

    @Test
    void shouldRecognizeMagnumAlias() {
        var result = recognitionService.recognize("MAGNUM CASH&CARRY");

        assertThat(result.rawMerchant()).isEqualTo("MAGNUM CASH&CARRY");
        assertThat(result.normalizedMerchant()).isEqualTo("MAGNUM CASH&CARRY");
        assertThat(result.merchantMatch().matched()).isTrue();
        assertThat(result.merchantMatch().displayName()).isEqualTo("MAGNUM");
    }

    @Test
    void shouldRecognizeYandexAlias() {
        var result = recognitionService.recognize("YANDEX.GO");

        assertThat(result.normalizedMerchant()).isEqualTo("YANDEX.GO");
        assertThat(result.merchantMatch().matched()).isTrue();
        assertThat(result.merchantMatch().displayName()).isEqualTo("YANDEX GO");
    }

    @Test
    void shouldNotRecognizeUnknownMerchant() {
        var result = recognitionService.recognize("UNKNOWN SHOP");

        assertThat(result.normalizedMerchant()).isEqualTo("UNKNOWN SHOP");
        assertThat(result.merchantMatch().matched()).isFalse();
    }

    @Test
    void shouldReturnNotMatchedForEmptyValue() {
        var result = recognitionService.recognize("   ");

        assertThat(result.normalizedMerchant()).isEmpty();
        assertThat(result.merchantMatch().matched()).isFalse();
    }

    @Test
    void shouldReturnNotMatchedForNullValue() {
        var result = recognitionService.recognize(null);

        assertThat(result.rawMerchant()).isNull();
        assertThat(result.normalizedMerchant()).isEmpty();
        assertThat(result.merchantMatch().matched()).isFalse();
    }

    private MerchantRecognitionService createRecognitionService() {
        MerchantNormalizer merchantNormalizer = new DefaultMerchantNormalizer();
        MerchantCatalog merchantCatalog = new InMemoryMerchantRepository();
        MerchantAliasCatalog merchantAliasCatalog = new InMemoryMerchantAliasCatalog();
        MerchantResolver merchantResolver = new InMemoryMerchantResolver(merchantCatalog, merchantAliasCatalog);
        return new DefaultMerchantRecognitionService(merchantNormalizer, merchantResolver);
    }
}
