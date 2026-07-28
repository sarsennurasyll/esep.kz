package com.esep.merchantmanagement.interfaces;

import com.esep.merchantmanagement.model.MerchantAliasMatchCommand;

/**
 * Command-порт сохранения подтверждённых алиасов продавцов.
 */
public interface MerchantAliasMatchCatalog {

    void save(MerchantAliasMatchCommand command);
}
