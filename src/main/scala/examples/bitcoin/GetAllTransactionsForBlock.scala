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
  package com.madewithtea.blockchainrpc.examples.bitcoin

  import cats.effect.{ExitCode, IO, IOApp}
  import scala.concurrent.ExecutionContext.global

  import com.madewithtea.blockchainrpc.{RPCClient, Config}
  import com.madewithtea.blockchainrpc.bitcoin.Syntax._

  object GetAllTransactionsFromBlock extends IOApp {

    def run(args: List[String]): IO[ExitCode] = {
      implicit val ec = global
      implicit val config = Config.fromEnv
      RPCClient
        .bitcoin(config.hosts, config.port, config.username, config.password)
        .use { rpc =>
          for {
            block <- rpc.getBlockByHash(
                "0000000000000000000759de6ab39c2d8fb01e4481ba581761ddc1d50a57358d"
              )
            txs <- rpc.getTransactions(block.tx)
            _ <- IO(println(s"Fetched ${txs.seq.size} transactions"))
          } yield ExitCode(0)
        }
    }
  }
