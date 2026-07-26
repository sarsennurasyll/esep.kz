package com.esep.merchantrecognition.interfaces;

import com.esep.merchantrecognition.model.MerchantRecognitionResult;

/**
 * Контракт комплексного распознавания продавца по исходному названию.
 */
public interface MerchantRecognitionService {

    MerchantRecognitionResult recognize(String rawMerchant);
}
