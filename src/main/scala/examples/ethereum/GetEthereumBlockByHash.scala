/**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.madewithtea.blockchainrpc.examples.ethereum

import cats.effect.{ExitCode, IO, IOApp}
import scala.concurrent.ExecutionContext.global

import com.madewithtea.blockchainrpc.{RPCClient, Config}
import com.madewithtea.blockchainrpc.ethereum.Syntax._
import com.madewithtea.blockchainrpc.ethereum.HexTools

object GetEthereumBlockByHash extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {


    implicit val ec = global
    implicit val config = Config.fromEnv
    RPCClient
      .ethereum(
        config.hosts,
        config.port,
        config.username,
        config.password,
        onErrorRetry = { (_, e: Throwable) =>
          IO(println(e))
        }
      )
      .use { ethereum =>
        for {
          block <- ethereum.getBlockByHash(
            "0x3bad41c70c9efac92490e8a74ab816558bbdada0984f2bcfa4cb1522ddb3ca16"
          )
          _ <- IO { println(s"block ${HexTools.parseQuantity(block.number)}: $block") }
        } yield ExitCode(0)
      }
  }
}
