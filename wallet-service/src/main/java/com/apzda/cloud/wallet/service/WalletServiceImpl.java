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
package com.apzda.cloud.wallet.service;

import com.apzda.cloud.wallet.proto.TradeDTO;
import com.apzda.cloud.wallet.proto.TransactionVO;
import com.apzda.cloud.wallet.proto.WalletDTO;
import com.apzda.cloud.wallet.proto.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private com.apzda.cloud.wallet.domain.service.WalletService walletService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransactionVO trade(TradeDTO request) {
        val wallet = walletService.openWallet(request.getUid(), request.getCurrency());
        val transaction = wallet.newTransaction(request);

        walletService.trade(wallet, transaction);

        return null;
    }

    @Override
    public WalletDTO wallet(WalletDTO request) {
        return null;
    }

}
