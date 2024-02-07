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
package com.apzda.cloud.wallet.domain.service;

import cn.hutool.core.date.DateUtil;
import com.apzda.cloud.wallet.config.WalletConfig;
import com.apzda.cloud.wallet.domain.entity.ChangeLog;
import com.apzda.cloud.wallet.domain.entity.Transaction;
import com.apzda.cloud.wallet.domain.entity.Wallet;
import com.apzda.cloud.wallet.domain.mapper.WalletMapper;
import com.apzda.cloud.wallet.error.WalletError;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@Service
@Slf4j
@RequiredArgsConstructor
public class WalletService extends ServiceImpl<WalletMapper, Wallet> {

    private final TransactionService transactionService;

    private final ChangeLogService changeLogService;

    @Transactional(rollbackFor = Exception.class)
    public synchronized Wallet openWallet(@NonNull Long uid, @NonNull String currency) {
        WalletConfig.getCurrencyConfig(currency);

        var wallet = baseMapper.openWallet(uid, currency);

        if (wallet == null) {
            wallet = new Wallet();
            val current = DateUtil.current();
            wallet.setCreatedAt(current);
            wallet.setUpdatedAt(current);
            wallet.setCreatedBy(String.valueOf(uid));
            wallet.setUpdatedBy(wallet.getCreatedBy());
            wallet.setUid(uid);
            wallet.setCurrency(currency);
            try {
                if (!save(wallet)) {
                    throw new IllegalStateException(
                            "Cannot open wallet for user(uid: " + uid + ", currency: " + currency + ")");
                }
                wallet = baseMapper.openWallet(uid, currency);
                val changeLog = ChangeLog.init(wallet);
                wallet.setBlock(changeLog.getBlock());

                if (!updateById(wallet)) {
                    throw new IllegalStateException(
                            "Cannot update wallet block for user(uid: " + uid + ", currency: " + currency + ")");
                }

                if (!changeLogService.save(changeLog)) {
                    throw new IllegalStateException(
                            "Cannot init wallet for user(uid: " + uid + ", currency: " + currency + ")");
                }
            }
            catch (DuplicateKeyException de) {
                log.warn("Parallel open wallet of user(uid: {}, currency: {}), try open it again", uid, currency);
                wallet = baseMapper.openWallet(uid, currency);
            }
        }

        if (wallet == null) {
            WalletError.NOTFOUND.emit(uid, currency);
        }

        return wallet;
    }

    @Transactional(rollbackFor = Exception.class)
    @Valid
    public Transaction trade(@NonNull final Wallet wallet, @NonNull final Transaction transaction) {
        transaction.setUid(wallet.getUid());
        val uid = transaction.getUid();
        transaction.setCurrency(wallet.getCurrency());
        val currency = transaction.getCurrency();

        if (wallet.isLocked()) {
            log.error("Wallet(uid: {}, currency: {}) is locked!", uid, currency);
            WalletError.LOCKED.emit(wallet);
        }

        val lastLog = getLastLog(wallet);
        // 完整性校验
        if (lastLog == null) {
            log.error("Wallet(uid: {}, currency: {}) change log not found!", uid, currency);
            WalletError.INTEGRITY_FAILED.emit(wallet);
        }
        if (!lastLog.getBlock().equals(wallet.getBlock())) {
            log.error("Wallet(uid: {}, currency: {}) integrity verification failed: log block({}) != wallet block({})",
                    uid, currency, lastLog.getBlock(), wallet.getBlock());
            WalletError.INTEGRITY_FAILED.emit(wallet);
        }

        // 钱包未开启过期机制
        if (!wallet.isExpireAble()) {
            transaction.setExpiredAt(null);
        }
        //

        ChangeLog changeLog = wallet.newChangeLog(transaction);

        changeLog.genBlock(wallet.getBlock());
        if (!changeLogService.save(changeLog)) {

        }
        return null;
    }

    @Nullable
    public ChangeLog getLastLog(@NonNull Wallet wallet) {
        return changeLogService.getLastLog(wallet.getUid(), wallet.getCurrency());
    }

}
