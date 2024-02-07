/*
 * Copyright (C) 2023-2024 Fengz Ning (windywany@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.apzda.cloud.wallet.domain.entity;

import cn.hutool.core.date.DateUtil;
import com.apzda.cloud.wallet.config.WalletConfig;
import com.apzda.cloud.wallet.config.WalletProperties;
import com.apzda.cloud.wallet.error.WalletError;
import com.apzda.cloud.wallet.proto.TradeDTO;
import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.val;
import org.springframework.lang.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@TableName("wallet")
@Data
public class Wallet implements Serializable {

    @Serial
    private static final long serialVersionUID = 1215576796903417299L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private Long createdAt;

    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    @TableLogic(delval = "1", value = "0")
    private boolean deleted;

    @NotNull
    private Long uid;

    @NotNull
    private String currency;

    @NotNull
    private Long amount;

    @NotNull
    private Long balance;

    @NotNull
    private Long withdrawal;

    @NotNull
    private Long frozen;

    @NotNull
    private Long outlay;

    private boolean locked;

    private String block;

    public boolean isExpireAble() {
        val config = WalletConfig.getCurrencyConfig(currency);
        return config.isEnabledExpire();
    }

    public double getRate() {
        val config = WalletConfig.getCurrencyConfig(currency);
        return config.getRate();
    }

    public short getScale() {
        val config = WalletConfig.getCurrencyConfig(currency);
        return config.getScale();
    }

    public short getPrecision() {
        val config = WalletConfig.getCurrencyConfig(currency);
        return config.getPrecision();
    }

    public boolean isWithdrawAble() {
        val config = WalletConfig.getCurrencyConfig(currency);
        return config.isWithdrawAble();
    }

    public ChangeLog newChangeLog(@NonNull Transaction transaction) {
        val amount = transaction.getAmount();
        val changeLog = new ChangeLog();
        changeLog.setUid(transaction.getUid());
        changeLog.setCurrency(transaction.getCurrency());
        return changeLog;
    }

    public Transaction newTransaction(@NonNull TradeDTO tradeDTO) {
        if (uid != tradeDTO.getUid() || currency.equals(tradeDTO.getCurrency())) {
            WalletError.TRADE_NOT_ALLOWED.emit(this);
        }
        val config = WalletConfig.getCurrencyConfig(currency);

        WalletProperties.BizSubject bizSubject = config.getBizSubject(currency, tradeDTO.getBiz(),
                tradeDTO.getBizSubject());

        val outlay = tradeDTO.getOutlay();
        if (outlay && !bizSubject.isOutlay()) {
            WalletError.OUTLAY_NOT_ALLOWED.emit(this);
        }
        if (!outlay && !bizSubject.isIncome()) {
            WalletError.INCOME_NOT_ALLOWED.emit(this);
        }
        val transaction = new Transaction();
        val current = DateUtil.current();
        transaction.setCreatedAt(current);
        transaction.setUpdatedAt(current);
        transaction.setUid(uid);
        transaction.setCurrency(currency);
        val amount = BigDecimal.valueOf(tradeDTO.getAmount())
            .multiply(BigDecimal.valueOf(getPrecision()))
            .toBigInteger()
            .longValue();
        transaction.setAmount(amount);
        transaction.setOutlay(outlay);
        transaction.setBizId(tradeDTO.getBizId());
        transaction.setBiz(tradeDTO.getBiz());
        transaction.setBizSubject(tradeDTO.getBizSubject());
        if (outlay) {// 支出判断是否需要冻结
            transaction.setNeedFrozen(bizSubject.isNeedFrozen());
        }
        else {
            // 收入判断是否可以提现
            transaction.setWithdrawAble(bizSubject.isWithdrawAble());
        }
        return transaction;
    }

}
