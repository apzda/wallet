package com.apzda.cloud.wallet.domain.service;

import com.apzda.cloud.gsvc.autoconfigure.MyBatisPlusAutoConfiguration;
import com.apzda.cloud.wallet.domain.entity.Transaction;
import com.apzda.cloud.wallet.test.TestApp;
import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@MybatisPlusTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(MyBatisPlusAutoConfiguration.class)
@ContextConfiguration(classes = TestApp.class)
@ActiveProfiles({ "test", "flyway" })
class WalletServiceTest {

    @Autowired
    private WalletService walletService;

    @Test
    public void user_wallet_should_be_opened() {
        // given
        val uid = 1L;
        val currency = "CNY";

        // when
        val wallet = walletService.openWallet(uid, currency);
        val lastLog = walletService.getLastLog(wallet);
        // then
        assertThat(wallet).isNotNull();
        assertThat(wallet.getPrecision()).isEqualTo(Short.valueOf("8"));
        assertThat(lastLog).isNotNull();
        assertThat(wallet.getBlock()).isEqualTo(lastLog.getBlock());
    }

    @Test
    void income_trade_should_be_ok() {
        // given
        val uid = 1L;
        val currency = "CNY";
        val transaction = new Transaction();
        val wallet = walletService.openWallet(uid, currency);

        // when
        val trans = walletService.trade(wallet, transaction);
        val lastLog = walletService.getLastLog(wallet);
        // then
        assertThat(trans).isNotNull();
        assertThat(lastLog).isNotNull();
    }

}
