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
package com.apzda.cloud.wallet.server;

import com.apzda.cloud.wallet.config.WalletConfig;
import com.apzda.cloud.wallet.proto.WalletServiceGsvc;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import java.lang.annotation.*;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@PropertySource("classpath:apzda.wallet.service.properties")
@Import({ WalletConfig.class, WalletServiceGsvc.class })
@ComponentScan(basePackages = { "com.apzda.cloud.wallet.**.service" })
@MapperScan(basePackages = { "com.apzda.cloud.wallet.domain.mapper" })
@Documented
public @interface EnableWalletServer {

}
